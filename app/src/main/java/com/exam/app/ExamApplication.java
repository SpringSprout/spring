package com.exam.app;

import com.spring.sprout.context.ApplicationContext;
import com.spring.sprout.context.ClassScanner;
import com.spring.sprout.context.Environment;
import com.spring.sprout.context.ResourcePatternResolver;

public class ExamApplication {

    public static void main(String[] args) {
        Environment env = new Environment();
        ResourcePatternResolver scanner = new ClassScanner();
        ApplicationContext context = new ApplicationContext(env, scanner);

        String basePackage = env.getProperty("scan.base-package");

        context.scan(basePackage);
    }

}
