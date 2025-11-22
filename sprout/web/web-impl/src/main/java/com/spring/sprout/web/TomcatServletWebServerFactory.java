package com.spring.sprout.web;

import com.spring.sprout.global.annotation.Component;
import com.spring.sprout.web.api.ServletWebServerFactory;
import com.spring.sprout.web.api.WebServer;
import java.io.File;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

@Component
public class TomcatServletWebServerFactory implements ServletWebServerFactory {

    private final int PORT = 8080;

    @Override
    public WebServer getWebServer() {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.getConnector();
        Context context = tomcat.addContext("", new File(".").getAbsolutePath());
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        Tomcat.addServlet(context, "dispatcher", dispatcherServlet);
        context.addServletMappingDecoded("/", "dispatcher");
        return new TomcatWebServer(tomcat);
    }
}
