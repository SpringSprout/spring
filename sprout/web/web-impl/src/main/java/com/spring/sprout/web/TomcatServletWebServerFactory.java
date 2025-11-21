package com.spring.sprout.web;

import com.spring.sprout.annotation.Component;
import com.spring.sprout.web.api.ServletWebServerFactory;
import com.spring.sprout.web.api.WebServer;
import com.sun.net.httpserver.HttpHandler;

@Component
public class TomcatServletWebServerFactory implements ServletWebServerFactory {

    @Override
    public WebServer getWebServer(HttpHandler httpHandler) {
        return null;
    }
}
