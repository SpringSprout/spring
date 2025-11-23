package com.spring.sprout.core.context;


import com.spring.sprout.core.beanfactory.BeanFactory;
import com.spring.sprout.core.io.Resource;
import com.spring.sprout.core.io.ResourcePatternResolver;
import com.spring.sprout.global.error.ErrorMessage;
import com.spring.sprout.global.error.SpringException;

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

                if (clazz.isAnnotation() || clazz.isInterface()) {
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