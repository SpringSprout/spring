package com.spring.sprout;


import com.spring.sprout.context.ApplicationContext;
import com.spring.sprout.context.Environment;
import com.spring.sprout.io.ResourcePatternResolver;

public class SpringApplication {

    public static ApplicationContext run(Class<?> mainClass) {
        Environment environment = new Environment();
        ResourcePatternResolver scanner = new ResourcePatternResolver();

        ApplicationContext context = new ApplicationContext(environment, scanner);

        context.registerSingleton("environment", environment);
        
        try {
            String basePackage = environment.getProperty("scan.base-package");

            if (basePackage == null || basePackage.isBlank()) {
                basePackage = mainClass.getPackageName();
            }
            context.scan(basePackage);
            context.refresh();
            return context;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Spring Application Start Failed", e);
        }
    }
}
