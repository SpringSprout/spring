package com.spring.sprout;


import com.spring.sprout.context.ApplicationContext;
import com.spring.sprout.context.EnvironmentImpl;
import com.spring.sprout.io.ResourcePatternResolver;

public class SpringApplication {

    private static final String CONFIG_BASE_PACKAGE = "com.spring.sprout.data.config";

    public static ApplicationContext run(Class<?> mainClass) {

        EnvironmentImpl environment = new EnvironmentImpl();
        ResourcePatternResolver scanner = new ResourcePatternResolver();

        ApplicationContext context = new ApplicationContext(environment, scanner);

        context.registerSingleton("environment", environment); // 수동 등록

        try {
            String basePackage = environment.getProperty("scan.base-package");

            if (basePackage == null || basePackage.isBlank()) {
                basePackage = mainClass.getPackageName();
            }
            context.scan(basePackage);
            context.scan(CONFIG_BASE_PACKAGE);
            context.refresh();
            return context;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Spring Application Start Failed", e);
        }
    }
}
