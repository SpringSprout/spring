package com.spring.sprout.context;


import com.spring.sprout.annotation.Component;
import com.spring.sprout.beanfactory.BeanFactory;
import com.spring.sprout.error.ErrorMessage;
import com.spring.sprout.error.SpringException;
import com.spring.sprout.io.Resource;
import com.spring.sprout.io.ResourcePatternResolver;

public class ApplicationContext extends BeanFactory {

    private final EnvironmentImpl environment;
    private final ResourcePatternResolver scanner;

    public ApplicationContext(EnvironmentImpl environment, ResourcePatternResolver scanner) {
        this.environment = environment;
        this.scanner = scanner;
    }

    public void scan(String basePackage) {
        Resource[] resources = scanner.getResources(basePackage);
        for (Resource resource : resources) {
            try {
                String className = convertPathToClassName(resource.getPath());
                Class<?> clazz = getClassLoader().loadClass(className);

                if (clazz.isAnnotationPresent(Component.class)) {
                    super.registerBeanClass(clazz);
                }
            } catch (Exception e) {
                throw new SpringException(ErrorMessage.BEAN_SCAN_FAILED);
            }
        }
    }

    public void refresh() {
        super.preInstantiateSingletons();
    }


    private String convertPathToClassName(String path) {
        return path.replace(".class", "").replace("/", ".");
    }

    public ClassLoader getClassLoader() {
        return this.scanner.getClassLoader();
    }
}