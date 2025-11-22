package com.spring.sprout.web;

import com.spring.sprout.web.api.WebServer;
import lombok.AllArgsConstructor;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;


@AllArgsConstructor
public class TomcatWebServer implements WebServer {

    private final Tomcat tomcat;

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
