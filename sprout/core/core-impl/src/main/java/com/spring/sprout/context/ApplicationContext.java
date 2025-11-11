package com.spring.sprout.context;

import static com.spring.sprout.error.ErrorMessage.NO_BEAN_FOUND_WITH_NAME;
import static com.spring.sprout.error.ErrorMessage.NO_BEAN_FOUND_WITH_TYPE;
import static com.spring.sprout.error.ErrorMessage.NO_UNIQUE_BEAN_FOUND_WITH_TYPE;

import com.spring.sprout.error.SpringException;
import java.util.HashMap;
import java.util.Map;

public class ApplicationContext implements BeanFactory, EnvironmentCapable {

    private Map<String, Object> beanRegistry = new HashMap<>();
    private Environment environment;

    public ApplicationContext(Environment environment) {
        this.environment = environment;
    }

    // BeanFactory
    @Override
    public Object getBean(String name) {
        Object bean = beanRegistry.get(name);
        if (bean == null) {
            throw new SpringException(NO_BEAN_FOUND_WITH_NAME);
        }
        return bean;
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        Map<String, T> matchingBeans = getBeansOfType(requiredType);
        if (matchingBeans.isEmpty()) {
            throw new SpringException(NO_BEAN_FOUND_WITH_TYPE);
        }
        if (matchingBeans.size() > 1) {
            throw new SpringException(NO_UNIQUE_BEAN_FOUND_WITH_TYPE);
        }
        return matchingBeans.values().iterator().next();
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) {
        Map<String, T> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : beanRegistry.entrySet()) {
            if (type.isInstance(entry.getValue())) {
                result.put(entry.getKey(), (T) entry.getValue());
            }
        }
        return result;
    }

    // EnvironmentCapable
    @Override
    public Environment getEnvironment() {
        return this.environment;
    }
}