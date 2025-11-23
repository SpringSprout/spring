package com.spring.sprout.core.beanfactory.support;

import com.spring.sprout.global.annotation.Component;

public class BeanNameGenerator {

    public String determineBeanName(Class<?> clazz) {
        Component component = clazz.getAnnotation(Component.class);
        String value = component.value();

        if (value != null && !value.isEmpty()) {
            return value;
        }
        String className = clazz.getSimpleName();
        return decapitalize(className);
    }

    private String decapitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) &&
            Character.isUpperCase(name.charAt(0))) {
            return name;
        }
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}
