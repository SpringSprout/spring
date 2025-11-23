package com.spring.sprout.bundle;

import com.spring.sprout.core.context.ApplicationContext;
import com.spring.sprout.core.context.EnvironmentImpl;
import com.spring.sprout.core.io.ResourcePatternResolver;
import com.spring.sprout.web.api.WebServer;

public class SpringApplication {

    private static final String DATA_CONFIG_BASE_PACKAGE = "com.spring.sprout";

    public static void run(Class<?> mainClass) {
        printBanner();
        EnvironmentImpl environment = new EnvironmentImpl();
        ResourcePatternResolver scanner = new ResourcePatternResolver();

        ApplicationContext context = new ApplicationContext(environment, scanner);

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
    }

    private static void startWebServer(ApplicationContext context) {
        WebServer webServer = context.getBean(WebServer.class);
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
              __  __       _____          _                  _____            _             
             |  \\/  |     / ____|        | |                / ____|          (_)            
             | \\  / |_  _| |    _   _ ___| |_ ___  _ __ ___| (___  _ __  _ __ _ _ __   __ _ 
             | |\\/| | | | | |   | | | / __| __/ _ \\| '_ ` _ \\\\___ \\| '_ \\| '__| | '_ \\ / _` |
             | |  | | |_| | |___| |_| \\__ \\ || (_) | | | | | |___) | |_) | |  | | | | | (_| |
             |_|  |_|\\__, |\\_____\\__,_|___/\\__\\___/|_| |_| |_|____/| .__/|_|  |_|_| |_|\\__, |
                      __/ |                                        | |                  __/ |
                     |___/                                         |_|                 |___/ 
            
             :: MyCustomSpring ::             (v1.0.0)
            """);
    }
}
