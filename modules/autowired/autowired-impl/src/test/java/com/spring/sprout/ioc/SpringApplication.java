package com.spring.sprout.ioc;

import SpringSprout.annotation.Autowired;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SpringApplication {

    private static BeanFactory beanFactory;

    public static void run() {
        beanFactory = new BeanFactory();
        try {
            injectDependency();
        } catch (Exception e) {
            throw new RuntimeException("Springboot load failed");
        }
    }

    private static void injectDependency() throws Exception {
        injectByConstructor();
        injectByAutowired();
    }

    private static void injectByConstructor() throws Exception {
        for (Object bean : beanFactory.getBeans().values()) {
            Class<?> clazz = bean.getClass();
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Constructor<?> targetConstructor = null;
            for (Constructor constructor : constructors) {
                if (constructor.isAnnotationPresent(Autowired.class)) {
                    if (targetConstructor != null) {
                        throw new RuntimeException("Autowired 생성자는 여러개가 존재할 수 없습니다");
                    }
                    targetConstructor = constructor;
                }
            }
            Class<?>[] parameterTypes = targetConstructor.getParameterTypes();
            Object[] args = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                Object dependency = beanFactory.getBeans().get(parameterType);
                args[i] = dependency;
            }
            beanFactory.getBeans()
                .put(targetConstructor.newInstance(args), targetConstructor.newInstance(args));
        }
    }

    private static void injectByAutowired() throws IllegalAccessException {
        for (Object bean : beanFactory.getBeans().values()) {
            Field[] fields = bean.getClass().getDeclaredFields();
            List<Field> autowiredFields = new ArrayList<>();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(Autowired.class)) {
                    continue;
                }
                autowiredFields.add(field);
            }
            for (Field field : autowiredFields) {
                Object toInjectBean = beanFactory.getBeans().get(field.getType());
                field.setAccessible(true);
                field.set(bean, toInjectBean);
            }
        }
    }
}
