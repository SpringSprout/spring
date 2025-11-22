package com.spring.sprout.core;

import com.spring.sprout.core.context.ApplicationContext;
import com.spring.sprout.core.context.EnvironmentImpl;
import com.spring.sprout.core.io.ResourcePatternResolver;
import com.spring.sprout.web.api.WebServer;

public class SpringApplication {

    private static final String DATA_CONFIG_BASE_PACKAGE = "com.spring.sprout";

    public static ApplicationContext run(Class<?> mainClass) {

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
            WebServer webServer = context.getBean(WebServer.class);
            webServer.start();
            return context;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Spring Application Start Failed", e);
        }
    }
}
