package com.spring.sprout.bundle.context;


import com.spring.sprout.bundle.beanfactory.DefaultBeanFactory;
import com.spring.sprout.bundle.io.Resource;
import com.spring.sprout.bundle.io.ResourcePatternResolver;
import com.spring.sprout.global.error.ErrorMessage;
import com.spring.sprout.global.error.SpringException;

public class SproutApplicationContext extends DefaultBeanFactory {

    private final ResourcePatternResolver scanner;

    public SproutApplicationContext(ResourcePatternResolver scanner) {
        this.scanner = scanner;
    }

    public void scan(String basePackage) {
        Resource[] resources = scanner.getResources(basePackage);
        for (Resource resource : resources) {
            try {
                String className = convertPathToClassName(resource.getPath());
                Class<?> clazz = getClassLoader().loadClass(className);

                if (clazz.isAnnotation()) {
                    continue;
                }

                if (hasComponentAnnotation(clazz)) {
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

