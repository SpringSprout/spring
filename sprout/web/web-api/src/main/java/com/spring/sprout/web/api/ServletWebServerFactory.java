package com.spring.sprout.web.api;

import com.sun.net.httpserver.HttpHandler;

public interface ServletWebServerFactory {

    WebServer getWebServer(HttpHandler httpHandler);
}
