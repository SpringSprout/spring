package com.spring.sprout.web;

import com.spring.sprout.global.annotation.Component;
import com.spring.sprout.web.api.WebServer;
import java.io.File;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.AbstractProtocol;


@Component
public class TomcatWebServer implements WebServer {

    private final int PORT = 8080;

    private final Tomcat tomcat;

    public TomcatWebServer(DispatcherServlet dispatcherServlet) {
        this.tomcat = new Tomcat();

        tomcat.setPort(PORT);
        Connector connector = tomcat.getConnector();
        AbstractProtocol<?> protocol = (AbstractProtocol<?>) connector.getProtocolHandler();

        // 최대 스레드 개수
        protocol.setMaxThreads(50);
        // 최소 스레드 개수
        protocol.setMinSpareThreads(10);
        // 최대 대기열 크기
        protocol.setAcceptCount(100);

        Context context = tomcat.addContext("", new File(".").getAbsolutePath());

        Tomcat.addServlet(context, "dispatcher", dispatcherServlet);
        context.addServletMappingDecoded("/", "dispatcher");
    }

    @Override
    public void start() {
        try {
            tomcat.start();
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
