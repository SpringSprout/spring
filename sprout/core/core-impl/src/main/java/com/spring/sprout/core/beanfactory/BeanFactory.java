package com.spring.sprout.core.beanfactory;

import static com.spring.sprout.global.error.ErrorMessage.NO_BEAN_FOUND_WITH_NAME;
import static com.spring.sprout.global.error.ErrorMessage.NO_BEAN_FOUND_WITH_TYPE;
import static com.spring.sprout.global.error.ErrorMessage.NO_UNIQUE_BEAN_FOUND_WITH_TYPE;

import com.spring.sprout.core.beanfactory.support.BeanNameGenerator;
import com.spring.sprout.global.annotation.Autowired;
import com.spring.sprout.global.error.ErrorMessage;
import com.spring.sprout.global.error.SpringException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BeanFactory {

    private final Set<Class<?>> componentClasses = new HashSet<>();
    private final BeanNameGenerator beanNameGenerator = new BeanNameGenerator();
    private Map<String, Object> singletonObjects = new HashMap<>();

    public void registerBeanClass(Class<?> clazz) {
        componentClasses.add(clazz);
    }

    public void preInstantiateSingletons() {
        for (Class<?> clazz : componentClasses) {
            createBean(clazz);
        }
    }

    public void registerSingleton(String name, Object singletonObject) {
        if (singletonObjects.containsKey(name)) {
            return;
        }
        singletonObjects.put(name, singletonObject);
    }

    private Object createBean(Class<?> clazz) {
        String beanName = beanNameGenerator.determineBeanName(clazz);

        if (singletonObjects.containsKey(beanName)) {
            return singletonObjects.get(beanName);
        }

        try {
            Object instance = instantiateBean(clazz);
            singletonObjects.put(beanName, instance);
            injectFields(instance);
            return instance;
        } catch (Exception e) {
            throw new SpringException(ErrorMessage.BEAN_CREATION_FAILED);
        }
    }

    private Object instantiateBean(Class<?> clazz) throws Exception {
        Constructor<?> targetConstructor = null;
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                if (targetConstructor != null) {
                    throw new SpringException(ErrorMessage.NOT_UNIQUE_AUTOWIRED);
                }
                targetConstructor = constructor;
            }
        }

        if (targetConstructor == null) {
            return clazz.getDeclaredConstructor().newInstance();
        }

        Class<?>[] parameterTypes = targetConstructor.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            args[i] = getBean(parameterTypes[i]);
        }

        return targetConstructor.newInstance(args);
    }

    private void injectFields(Object bean) throws IllegalAccessException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Object dependency = getBean(field.getType());
                field.setAccessible(true);
                field.set(bean, dependency);
            }
        }
    }


    public Object getBean(String name) {
        if (singletonObjects.containsKey(name)) {
            return singletonObjects.get(name);
        }

        for (Class<?> clazz : componentClasses) {
            String candidateName = beanNameGenerator.determineBeanName(clazz);
            if (candidateName.equals(name)) {
                return createBean(clazz);
            }
        }
        throw new SpringException(NO_BEAN_FOUND_WITH_NAME);
    }

    public <T> T getBean(Class<T> requiredType) {
        Map<String, T> matchingBeans = getBeansOfType(requiredType);
        if (!matchingBeans.isEmpty()) {
            if (matchingBeans.size() > 1) {
                throw new SpringException(NO_UNIQUE_BEAN_FOUND_WITH_TYPE);
            }
            return matchingBeans.values().iterator().next();
        }

        for (Class<?> clazz : componentClasses) {
            if (requiredType.isAssignableFrom(clazz)) {
                return (T) createBean(clazz);
            }
        }

        throw new SpringException(NO_BEAN_FOUND_WITH_TYPE);
    }

    public <T> Map<String, T> getBeansOfType(Class<T> type) {
        Map<String, T> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : singletonObjects.entrySet()) {
            if (type.isInstance(entry.getValue())) {
                result.put(entry.getKey(), (T) entry.getValue());
            }
        }
        return result;
    }
}
