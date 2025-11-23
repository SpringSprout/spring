package com.spring.sprout.bundle;

import com.spring.sprout.bundle.context.EnvironmentImpl;
import com.spring.sprout.bundle.context.SproutApplicationContext;
import com.spring.sprout.bundle.io.ResourcePatternResolver;
import com.spring.sprout.web.api.WebServer;

public class SproutApplication {

    private static final String DATA_CONFIG_BASE_PACKAGE = "com.spring.sprout";

    public static SproutApplicationContext run(Class<?> mainClass) {
        printBanner();
        EnvironmentImpl environment = new EnvironmentImpl();
        ResourcePatternResolver scanner = new ResourcePatternResolver();

        SproutApplicationContext context = new SproutApplicationContext(scanner);
        context.registerSingleton("beanFactory", context);
        context.registerSingleton("environment", environment);

        try {
            String basePackage = environment.getProperty("scan.base-package");

            if (basePackage == null || basePackage.isBlank()) {
                basePackage = mainClass.getPackageName();
            }
            context.scan(basePackage);
            context.scan(DATA_CONFIG_BASE_PACKAGE);
            context.refresh();
            startWebServer(context);
            printAllThreads();
            System.out.println("Spring Application Started Successfully");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Spring Application Start Failed", e);
        }

        return context;
    }

    private static void startWebServer(SproutApplicationContext context) {
        WebServer webServer = context.getBean(WebServer.class);
        webServer.init();
        webServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println(">> Shutdown Signal Detected. Stopping WebServer...");
            webServer.stop();
            System.out.println(">> WebServer Stopped Successfully.");
        }));
    }

    private static void printAllThreads() {
        Thread.getAllStackTraces().keySet().forEach(thread ->
            System.out.println(
                "Thread: " + thread.getName() + " (Daemon: " + thread.isDaemon() + ")")
        );
    }

    private static void printBanner() {
        System.out.println("""
              _____                  _
             / ____|                 | |
            | (___  _ __  _ __ ___  _| |_
            \\___ \\| '_ \\| '__/ _ \\| | __|
             ____) | |_) | | | (_) | | |_
            |_____/| .__/|_|  \\___/ \\__|
                   | |
                   |_|
            
             :: Sprout ::             (v1.0.0)
            """);
    }
}
