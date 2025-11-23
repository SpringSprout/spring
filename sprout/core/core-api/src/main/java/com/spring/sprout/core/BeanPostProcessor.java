package com.spring.sprout.core;

public interface BeanPostProcessor {

    default Object postProcess(Object bean, String beanName) {
        return bean;
    }

}
