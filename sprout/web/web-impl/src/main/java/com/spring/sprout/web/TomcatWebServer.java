package com.spring.sprout.web;

import com.spring.sprout.global.annotation.Component;
import com.spring.sprout.web.api.WebServer;
import java.io.File;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;


@Component
public class TomcatWebServer implements WebServer {

    private final int PORT = 8080;

    private final Tomcat tomcat;

    public TomcatWebServer() {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.getConnector();
        Context context = tomcat.addContext("", new File(".").getAbsolutePath());
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        Tomcat.addServlet(context, "dispatcher", dispatcherServlet);
        context.addServletMappingDecoded("/", "dispatcher");
        this.tomcat = tomcat;
    }

    @Override
    public void start() {
        try {
            tomcat.start();
            Thread requestProcessingThread = new Thread(() -> tomcat.getServer().await());
            requestProcessingThread.setDaemon(false);
            requestProcessingThread.start();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            tomcat.stop();
            tomcat.destroy();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
    }
}
