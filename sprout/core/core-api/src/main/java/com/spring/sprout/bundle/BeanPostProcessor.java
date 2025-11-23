package com.spring.sprout.bundle;

public interface BeanPostProcessor {

    default Object postProcess(Object bean, String beanName) {
        return bean;
    }
}
