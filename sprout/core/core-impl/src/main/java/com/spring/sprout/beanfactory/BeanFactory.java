package com.spring.sprout.beanfactory;

import static com.spring.sprout.error.ErrorMessage.NO_BEAN_FOUND_WITH_NAME;
import static com.spring.sprout.error.ErrorMessage.NO_BEAN_FOUND_WITH_TYPE;
import static com.spring.sprout.error.ErrorMessage.NO_UNIQUE_BEAN_FOUND_WITH_TYPE;

import com.spring.sprout.annotation.Autowired;
import com.spring.sprout.error.ErrorMessage;
import com.spring.sprout.error.SpringException;
import com.spring.sprout.beanfactory.support.BeanNameGenerator;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BeanFactory {

    // 빈 인스턴스를 저장하는 저장소
    private Map<String, Object> singletonObjects = new HashMap<>();
    // 스캔된 클래스 정보를 저장
    private final Set<Class<?>> componentClasses = new HashSet<>();
    private final BeanNameGenerator beanNameGenerator = new BeanNameGenerator();

    public void registerBeanClass(Class<?> clazz) {
        componentClasses.add(clazz);
    }

    public void preInstantiateSingletons() {
        for (Class<?> clazz : componentClasses) {
            createBean(clazz);
        }
    }

    // 2 - 1 객체 생성 및 의존성 주입
    private Object createBean(Class<?> clazz) {
        String beanName = beanNameGenerator.determineBeanName(clazz);

        // 이미 생성된 빈이 있다면 반환 (싱글톤 보장 위함)
        if (singletonObjects.containsKey(beanName)) {
            return singletonObjects.get(beanName);
        }

        try {
            // A. 생성자 주입
            Object instance = instantiateBean(clazz);
            singletonObjects.put(beanName, instance); // 조기 등록
            // B. 필드 주입
            injectFields(instance);
            return instance;
        } catch (Exception e) {
            throw new SpringException(ErrorMessage.BEAN_CREATION_FAILED);
        }
    }

    // 생성자 주입
    private Object instantiateBean(Class<?> clazz) throws Exception {
        Constructor<?> targetConstructor = null;
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Autowired.class)) { // @Autowired 붙은 생성자 찾기
                if (targetConstructor != null) {
                    throw new SpringException(ErrorMessage.NOT_UNIQUE_AUTOWIRED);
                }
                targetConstructor = constructor;
            }
        }

        if (targetConstructor == null) { // 없을 경우 기본 생성자 사용
            return clazz.getDeclaredConstructor().newInstance();
        }

        // 생성자 파라미터 찾기
        Class<?>[] parameterTypes = targetConstructor.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            args[i] = getBean(parameterTypes[i]);
        }

        // 파라미터 주입 후 객체 생성
        return targetConstructor.newInstance(args);
    }

    // 필드 주입 메소드
    private void injectFields(Object bean) throws IllegalAccessException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Object dependency = getBean(field.getType()); // 의존성 조회
                field.setAccessible(true);
                field.set(bean, dependency); // 이미 생성된 싱글톤 주입
            }
        }
    }


    public Object getBean(String name) {
        // 1. 이미 있으면 싱글톤 객체 반환
        if (singletonObjects.containsKey(name)) {
            return singletonObjects.get(name);
        }

        // 2. 아직 생성되지 않았다면 생성 후 반환
        for (Class<?> clazz : componentClasses) {
            String candidateName = beanNameGenerator.determineBeanName(clazz);
            if (candidateName.equals(name)) {
                return createBean(clazz);
            }
        }
        throw new SpringException(NO_BEAN_FOUND_WITH_NAME); // 없는 경우 에러
    }

    public <T> T getBean(Class<T> requiredType) {
        // 1. 이미 있다면 빈 반환
        Map<String, T> matchingBeans = getBeansOfType(requiredType);
        if (!matchingBeans.isEmpty()) {
            if (matchingBeans.size() > 1) {
                throw new SpringException(NO_UNIQUE_BEAN_FOUND_WITH_TYPE);
            }
            return matchingBeans.values().iterator().next();
        }

        // 2. 아직 생성되지 않았다면 생성 후 반환
        for (Class<?> clazz : componentClasses) {
            if (requiredType.isAssignableFrom(clazz)) {
                return (T) createBean(clazz);
            }
        }

        throw new SpringException(NO_BEAN_FOUND_WITH_TYPE); // 없는 경우 에러
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
