package com.spring.sprout.context;


import com.spring.sprout.annotation.Component;
import com.spring.sprout.beanfactory.BeanFactory;
import com.spring.sprout.error.ErrorMessage;
import com.spring.sprout.error.SpringException;
import com.spring.sprout.io.Resource;
import com.spring.sprout.io.ResourcePatternResolver;

public class ApplicationContext extends BeanFactory {

    private final Environment environment;
    private final ResourcePatternResolver scanner;

    public ApplicationContext(Environment environment, ResourcePatternResolver scanner) {
        this.environment = environment;
        this.scanner = scanner;
    }

    // 1. 스캔 -> 어떤 클래스가 빈 대상인지 조사
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

    // 2. 리프레시 단계 -> 실제 빈을 생성 후 의존성 주입
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