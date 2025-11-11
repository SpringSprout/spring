package com.spring.sprout.context;

import java.util.Map;

public interface ListableBeanFactory {

    Object getBean(String beanName);

    <T> T getBean(Class<T> requiredType);

    <T> Map<String, T> getBeansOfType(Class<T> requiredType);

}
