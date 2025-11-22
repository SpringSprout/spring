package com.spring.sprout.web;

import com.spring.sprout.global.annotation.Component;
import com.spring.sprout.web.api.WebServer;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;


@Component
public class TomcatWebServer implements WebServer {

    @Override
    public WebServer getWeberServer() {
        return null;
    }

    @Override
    public void start() {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);

        try {
            tomcat.start();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
        tomcat.getServer().await();
    }
}
