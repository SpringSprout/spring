package com.spring.sprout.core.beanfactory;

import static com.spring.sprout.global.error.ErrorMessage.NO_BEAN_FOUND_WITH_NAME;
import static com.spring.sprout.global.error.ErrorMessage.NO_BEAN_FOUND_WITH_TYPE;
import static com.spring.sprout.global.error.ErrorMessage.NO_UNIQUE_BEAN_FOUND_WITH_TYPE;

import com.spring.sprout.core.beanfactory.support.BeanNameGenerator;
import com.spring.sprout.global.annotation.Autowired;
import com.spring.sprout.global.annotation.Component;
import com.spring.sprout.global.error.ErrorMessage;
import com.spring.sprout.global.error.SpringException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BeanFactory {

    private final Set<Class<?>> componentClasses = new HashSet<>();
    private final BeanNameGenerator beanNameGenerator = new BeanNameGenerator();
    private final Map<Class<? extends Annotation>, Boolean> componentAnnotationCache = new ConcurrentHashMap<>();
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
        Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
        if (declaredConstructors.length == 1) {
            targetConstructor = declaredConstructors[0];
        } else {
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if (constructor.isAnnotationPresent(Autowired.class)) {
                    if (targetConstructor != null) {
                        throw new SpringException(ErrorMessage.NOT_UNIQUE_AUTOWIRED);
                    }
                    targetConstructor = constructor;
                }
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

    /**
     * 클래스에 @Component가 있거나,
     *
     * @Component를 포함한 메타 어노테이션(@Service 등)이 있는지 확인
     */
    public boolean hasComponentAnnotation(Class<?> clazz) {
        // 1. 해당 클래스에 직접 @Component가 붙어있는지 확인 (가장 빠름)
        if (clazz.isAnnotationPresent(Component.class)) {
            return true;
        }

        // 2. 클래스에 붙은 어노테이션들을 하나씩 검사
        for (Annotation annotation : clazz.getAnnotations()) {
            // 각 어노테이션 타입이 Component인지 확인 (캐시 활용)
            if (isComponent(annotation.annotationType())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 특정 어노테이션 타입이 @Component를 포함하고 있는지 확인 (재귀 + 캐싱)
     */
    private boolean isComponent(Class<? extends Annotation> annotationType) {
        // 0. 기본 제외 패키지 (java.lang, kotlin 등)
        if (annotationType.getPackageName().startsWith("java.lang") ||
            annotationType.getPackageName().startsWith("kotlin")) {
            return false;
        }

        // 1. 캐시 확인 (있으면 바로 리턴)
        if (componentAnnotationCache.containsKey(annotationType)) {
            return componentAnnotationCache.get(annotationType);
        }

        // 2. 현재 어노테이션이 @Component를 가지고 있는지 확인
        if (annotationType.isAnnotationPresent(Component.class)) {
            componentAnnotationCache.put(annotationType, true);
            return true;
        }

        // 3. 메타 어노테이션 재귀 검사
        // (현재 어노테이션 위에 붙은 다른 어노테이션들을 검사)
        for (Annotation metaAnnotation : annotationType.getAnnotations()) {
            // 재귀 호출
            if (isComponent(metaAnnotation.annotationType())) {
                // 자식 중 하나라도 Component라면 나도 Component임
                componentAnnotationCache.put(annotationType, true);
                return true;
            }
        }

        // 4. 끝까지 찾아봤는데 없으면 false로 캐싱
        componentAnnotationCache.put(annotationType, false);
        return false;
    }
}
