package com.spring.sprout.context;

import static com.spring.sprout.error.ErrorMessage.NO_BEAN_FOUND_WITH_NAME;
import static com.spring.sprout.error.ErrorMessage.NO_BEAN_FOUND_WITH_TYPE;
import static com.spring.sprout.error.ErrorMessage.NO_UNIQUE_BEAN_FOUND_WITH_TYPE;

import com.spring.sprout.annotation.Component;
import com.spring.sprout.error.ErrorMessage;
import com.spring.sprout.error.SpringException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ApplicationContext implements BeanFactory, EnvironmentCapable {

    private Map<String, Object> beanRegistry = new HashMap<>();
    private Environment environment;
    private final ResourcePatternResolver scanner;

    public ApplicationContext(Environment environment, ResourcePatternResolver scanner) {
        this.environment = environment;
        this.scanner = scanner;
    }

    // ResourcePatternResolver - Bean Scan

    public void scan(String basePackage) {
        Resource[] resources = scanner.getResources(basePackage);
        for (Resource resource : resources) {
            try {
                String className = convertPathToClassName(resource.getPath());
                Class<?> clazz = getClassLoader().loadClass(className);

                if (clazz.isAnnotationPresent(Component.class)) {
                    String beanName = determineBeanName(clazz);
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    registerBean(beanName, instance);
                }
            } catch (Exception e) {
                throw new SpringException(ErrorMessage.BEAN_SCAN_FAILED);
            }
        }
    }

    // ResourcePatternResolver - Register Bean

    public void registerBean(String name, Object bean) {
        if (beanRegistry.containsKey(name)) {
            throw new SpringException(ErrorMessage.BEAN_NAME_CONFLICT);
        }
        beanRegistry.put(name, bean);
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

    // --- 헬퍼 메소드 ---

    private String convertPathToClassName(String path) {
        return path.replace(".class", "").replace("/", ".");
    }

    public ClassLoader getClassLoader() {
        return this.scanner.getClassLoader();
    }

    private String determineBeanName(Class<?> clazz) {
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