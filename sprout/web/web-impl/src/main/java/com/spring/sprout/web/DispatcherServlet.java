package com.spring.sprout.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.sprout.bundle.beanfactory.BeanFactory;
import com.spring.sprout.global.annotation.Component;
import com.spring.sprout.global.annotation.controller.Controller;
import com.spring.sprout.global.annotation.controller.GetMapping;
import com.spring.sprout.global.annotation.controller.PostMapping;
import com.spring.sprout.global.error.ErrorMessage;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
public class DispatcherServlet extends HttpServlet {

    private final BeanFactory beanFactory;
    ObjectMapper objectMapper = new ObjectMapper();
    private Map<HandlerKey, Handler> handlerMapping = new HashMap<>();

    public DispatcherServlet(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }


    public void init() {
        registerHandlers();
    }

    private void registerHandlers() {
        Map<String, Object> allBeans = beanFactory.getAllBeans();

        for (Object bean : allBeans.values()) {
            Class<?> clazz = bean.getClass();
            if (!clazz.isAnnotationPresent(Controller.class)) {
                continue;
            }

            for (Method method : clazz.getMethods()) {
                String url = null;
                RequestMethod requestMethod = null; // 메서드 타입 저장 변수

                if (method.isAnnotationPresent(GetMapping.class)) {
                    url = method.getAnnotation(GetMapping.class).value();
                    requestMethod = RequestMethod.GET;
                } else if (method.isAnnotationPresent(PostMapping.class)) {
                    url = method.getAnnotation(PostMapping.class).value();
                    requestMethod = RequestMethod.POST;
                }

                if (url != null && requestMethod != null) {
                    HandlerKey handlerKey = new HandlerKey(url, requestMethod);
                    handlerMapping.put(handlerKey, (request, response) -> {
                        Object result = method.invoke(bean);

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

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String requestUri = req.getRequestURI();
        String requestMethodString = req.getMethod();
        HandlerKey handlerKey = new HandlerKey(
            requestUri,
            RequestMethod.valueOf(requestMethodString)
        );
        Handler handler = handlerMapping.get(handlerKey);

        if (handler == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.setContentType("application/json;charset=UTF-8");

            String jsonResponse = String.format("{\"message\": \"%s\"}",
                ErrorMessage.NOT_FOUND.getMessage());
            resp.getWriter().write(jsonResponse);
            return;
        }

        try {
            handler.handle(req, resp);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json;charset=UTF-8");

            resp.getWriter().write("{\"message\": \"Internal Server Error\"}");
            e.printStackTrace();
        }
    }

    private interface Handler {

        void handle(HttpServletRequest request, HttpServletResponse response) throws Exception;
    }
}
