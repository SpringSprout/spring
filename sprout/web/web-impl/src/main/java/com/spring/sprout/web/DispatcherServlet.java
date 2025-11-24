package com.spring.sprout.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.sprout.bundle.beanfactory.BeanFactory;
import com.spring.sprout.global.annotation.Component;
import com.spring.sprout.global.annotation.controller.Controller;
import com.spring.sprout.global.annotation.controller.GetMapping;
import com.spring.sprout.global.annotation.controller.PostMapping;
import com.spring.sprout.global.annotation.controller.RequestBody;
import com.spring.sprout.global.error.ErrorMessage;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * [프론트 컨트롤러 (Front Controller) 서블릿]
 *
 * <p>모든 클라이언트의 HTTP 요청을 중앙에서 받아, 적절한 {@link Controller}의 메서드로 위임(Dispatch)하는
 * 웹 애플리케이션의 핵심 진입점입니다.</p>
 *
 * <p>주요 역할:</p>
 * <ul>
 * <li><b>요청 라우팅 (Routing):</b> 요청 URI와 HTTP 메서드를 분석하여 처리할 핸들러를 찾습니다. (HandlerMapping)</li>
 * <li><b>핸들러 실행 (Execution):</b> 리플렉션을 통해 실제 컨트롤러의 비즈니스 로직을 호출합니다. (HandlerAdapter)</li>
 * <li><b>응답 처리 (View Rendering):</b> 핸들러의 반환값을 JSON으로 직렬화하여 응답 본문에 씁니다. (REST API 지원)</li>
 * <li><b>예외 처리 (Exception Handling):</b> 요청 처리 중 발생하는 예외를 잡아 적절한 HTTP 상태 코드(500 등)로 변환합니다.</li>
 * </ul>
 *
 * @see jakarta.servlet.http.HttpServlet
 */
@Component
public class DispatcherServlet extends HttpServlet {

    private final BeanFactory beanFactory;

    /**
     * JSON 직렬화를 위한 매퍼
     */
    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * URL+Method 조합을 키로 하고, 실행 로직(Handler)을 값으로 가지는 라우팅 테이블. 초기화 시점에 미리 구성되어 런타임 성능을 보장합니다.
     */
    private Map<HandlerKey, Handler> handlerMapping = new HashMap<>();

    public DispatcherServlet(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 서블릿 초기화 단계에서 호출됩니다. 컨테이너에 등록된 모든 빈을 검색하여 매핑 정보를 구축합니다.
     */
    public void init() {
        registerHandlers();
    }

    /**
     * BeanFactory에서 @Controller 빈을 찾아 매핑 정보를 메모리에 캐싱합니다.
     *
     * <p>작동 원리:</p>
     * <ol>
     * <li>모든 빈을 순회하며 @Controller 어노테이션이 있는지 확인합니다.</li>
     * <li>컨트롤러 내부의 메서드 중 @GetMapping, @PostMapping이 붙은 메서드를 찾습니다.</li>
     * <li>URL과 HTTP 메서드를 키로 하여, 해당 메서드를 실행하는 람다식을 핸들러 맵에 등록합니다.</li>
     * </ol>
     */
    private void registerHandlers() {
        Map<String, Object> allBeans = beanFactory.getAllBeans();

        for (Object bean : allBeans.values()) {
            Class<?> clazz = bean.getClass();
            if (!clazz.isAnnotationPresent(Controller.class)) {
                continue;
            }

            for (Method method : clazz.getMethods()) {
                String url = null;
                RequestMethod requestMethod = null;

                if (method.isAnnotationPresent(GetMapping.class)) {
                    url = method.getAnnotation(GetMapping.class).value();
                    requestMethod = RequestMethod.GET;
                } else if (method.isAnnotationPresent(PostMapping.class)) {
                    url = method.getAnnotation(PostMapping.class).value();
                    requestMethod = RequestMethod.POST;
                }

                if (url != null && requestMethod != null) {
                    HandlerKey handlerKey = new HandlerKey(url, requestMethod);

                    // 리플렉션 호출 로직을 람다로 캡슐화하여 등록
                    handlerMapping.put(handlerKey, (request, response) -> {
                        Parameter[] parameters = method.getParameters();
                        Object[] args = new Object[parameters.length];
                        for(int i = 0; i < parameters.length; i++) {
                            Parameter parameter = parameters[i];
                            if(parameter.isAnnotationPresent(RequestBody.class)){
                                args[i] =  objectMapper.readValue(request.getReader(), parameter.getType());
                            }
                        }

                        Object result = method.invoke(bean, args);

                        response.setContentType("application/json;charset=UTF-8");
                        if (result != null) {
                            String jsonResult = objectMapper.writeValueAsString(result);
                            response.getWriter().write(jsonResult);
                        }
                    });
                    System.out.println("Mapped URL path [" + url + "] to method [" + method + "]");
                }
            }
        }
    }

    /**
     * 실제 HTTP 요청이 들어올 때마다 실행되는 메인 메서드입니다. HttpServlet의 service 메서드를 오버라이딩하여 모든 요청(GET, POST 등)을
     * 처리합니다.
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String requestUri = req.getRequestURI();
        String requestMethodString = req.getMethod();

        // 요청 정보로 핸들러 키 생성
        HandlerKey handlerKey = new HandlerKey(
            requestUri,
            RequestMethod.valueOf(requestMethodString)
        );

        // 핸들러 조회 (HandlerMapping 역할)
        Handler handler = handlerMapping.get(handlerKey);

        // 매핑되는 핸들러가 없는 경우 404 처리
        if (handler == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.setContentType("application/json;charset=UTF-8");

            String jsonResponse = String.format("{\"message\": \"%s\"}",
                ErrorMessage.NOT_FOUND.getMessage());
            resp.getWriter().write(jsonResponse);
            return;
        }

        try {
            // 핸들러 실행 (비즈니스 로직 수행 및 응답 작성)
            handler.handle(req, resp);
        } catch (Exception e) {
            // 예외 발생 시 500 처리 (Global Exception Handling)
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json;charset=UTF-8");

            resp.getWriter().write("{\"message\": \"Internal Server Error\"}");
            e.printStackTrace();
        }
    }

    /**
     * 핸들러 실행을 위한 함수형 인터페이스. 람다식을 통해 메서드 호출 로직을 추상화합니다.
     */
    private interface Handler {

        void handle(HttpServletRequest request, HttpServletResponse response) throws Exception;
    }
}