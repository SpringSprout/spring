package com.spring.sprout.web;

import com.spring.sprout.global.error.ErrorMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

public class DispatcherServlet extends HttpServlet {

    private final HashMap<String, Handler> handlerMapping = new HashMap<>();

    public DispatcherServlet() {
        // 나중에는 @Controller, @RequestMapping 스캔해서 채우게 바꿀 수 있음
        registerHandlers();
    }

    private void registerHandlers() {
        handlerMapping.put("/", (request, response) -> {
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Hello from /");
        });
        handlerMapping.put("/hello", (request, response) -> {
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Hello from /hello");
        });
        handlerMapping.put("/health", (request, response) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":\"UP\"}");
        });
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        String path = req.getRequestURI();
        Handler handler = handlerMapping.get(path);

        if (handler == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().write(ErrorMessage.NOT_FOUND.getMessage());
            return;
        }

        try {
            handler.handle(req, resp);
        } catch (Exception e) {
            // 간단 예외 처리 – 나중에 @ControllerAdvice 같은 구조로 확장 가능
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().write("Internal Server Error");
            e.printStackTrace();
        }
    }


    private interface Handler {

        void handle(HttpServletRequest request, HttpServletResponse response) throws Exception;
    }
}