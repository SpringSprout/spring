package com.spring.sprout.bundle.beanfactory;

import java.util.Map;

public interface BeanFactory {

    Object getBean(String name);

    <T> T getBean(Class<T> requiredType);

    <T> Map<String, T> getBeansOfType(Class<T> type);
}