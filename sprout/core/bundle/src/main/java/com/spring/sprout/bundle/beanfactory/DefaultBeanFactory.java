package com.spring.sprout.bundle.beanfactory;

import static com.spring.sprout.global.error.ErrorMessage.NO_BEAN_FOUND_WITH_NAME;
import static com.spring.sprout.global.error.ErrorMessage.NO_BEAN_FOUND_WITH_TYPE;
import static com.spring.sprout.global.error.ErrorMessage.NO_UNIQUE_BEAN_FOUND_WITH_TYPE;

import com.spring.sprout.JdbcTemplate;
import com.spring.sprout.bundle.BeanPostProcessor;
import com.spring.sprout.bundle.beanfactory.support.BeanNameGenerator;
import com.spring.sprout.data.support.RepositoryHandler;
import com.spring.sprout.global.annotation.Autowired;
import com.spring.sprout.global.annotation.Component;
import com.spring.sprout.global.annotation.db.Repository;
import com.spring.sprout.global.error.ErrorMessage;
import com.spring.sprout.global.error.SpringException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * [기본 빈 팩토리 구현체]
 *
 * <p>IoC 컨테이너의 핵심 구현체로, 빈의 정의(Class), 생성, 의존성 주입, 생명주기 관리를 총괄합니다.
 * 싱글톤 레지스트리로서 기능하며, 등록된 모든 빈을 싱글톤 스코프로 관리합니다.</p>
 *
 * <p>주요 기능:</p>
 * <ul>
 * <li><b>의존성 주입 (DI):</b> 생성자 주입 및 필드 주입(@Autowired) 지원</li>
 * <li><b>순환 참조 해결:</b> 객체 생성 후 의존성 주입 전 미리 참조를 노출하는 전략 사용</li>
 * <li><b>자동 프록시 생성:</b> @Repository 인터페이스에 대한 동적 프록시 생성 지원</li>
 * <li><b>빈 후처리기(BPP):</b> {@link BeanPostProcessor}를 통한 빈 생성 후킹 및 변조 지원</li>
 * <li><b>메타 어노테이션 스캔:</b> @Component를 포함한 커스텀 어노테이션 인식</li>
 * </ul>
 *
 * @see BeanFactory
 * @see BeanPostProcessor
 */
public class DefaultBeanFactory implements BeanFactory {

    /**
     * 등록된 컴포넌트 클래스 메타정보 집합
     */
    protected final Set<Class<?>> componentClasses = new HashSet<>();

    /**
     * 빈 이름 생성 전략
     */
    private final BeanNameGenerator beanNameGenerator = new BeanNameGenerator();

    /**
     * 메타 어노테이션 분석 성능 향상을 위한 캐시
     */
    private final Map<Class<? extends Annotation>, Boolean> componentAnnotationCache = new ConcurrentHashMap<>();

    /**
     * 싱글톤 빈 인스턴스 저장소 (1차 캐시 역할)
     */
    private Map<String, Object> singletonObjects = new HashMap<>();

    /**
     * 등록된 빈 후처리기 목록
     */
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    @Override
    public Object getBean(String name) {
        if (singletonObjects.containsKey(name)) {
            return singletonObjects.get(name);
        }

        // 아직 생성되지 않은 빈이라면 클래스 정보를 찾아 생성 시도
        for (Class<?> clazz : componentClasses) {
            String candidateName = beanNameGenerator.determineBeanName(clazz);
            if (candidateName.equals(name)) {
                return createBean(clazz);
            }
        }
        throw new SpringException(NO_BEAN_FOUND_WITH_NAME);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        Map<String, T> matchingBeans = getBeansOfType(requiredType);
        if (!matchingBeans.isEmpty()) {
            if (matchingBeans.size() > 1) {
                throw new SpringException(NO_UNIQUE_BEAN_FOUND_WITH_TYPE);
            }
            return matchingBeans.values().iterator().next();
        }

        // 인스턴스가 없다면 클래스 정보에서 찾아 생성
        for (Class<?> clazz : componentClasses) {
            if (requiredType.isAssignableFrom(clazz)) {
                return (T) createBean(clazz);
            }
        }
        throw new SpringException(NO_BEAN_FOUND_WITH_TYPE);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) {
        Map<String, T> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : singletonObjects.entrySet()) {
            if (type.isInstance(entry.getValue())) {
                result.put(entry.getKey(), (T) entry.getValue());
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> getAllBeans() {
        return singletonObjects;
    }

    public void registerBeanClass(Class<?> clazz) {
        componentClasses.add(clazz);
    }

    /**
     * 등록된 모든 컴포넌트 클래스에 대해 싱글톤 인스턴스를 미리 생성합니다. (Eager Initialization) 애플리케이션 시작 시점에 호출되어야 합니다.
     */
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

    /**
     * 실제 빈 인스턴스를 생성하고 의존성을 주입하는 핵심 메서드입니다.
     *
     * <p>생성 단계:</p>
     * <ol>
     * <li>인스턴스화 (Instantiation)</li>
     * <li>싱글톤 레지스트리에 조기 등록 (순환 참조 해결을 위함)</li>
     * <li>의존성 주입 (Populate Properties)</li>
     * <li>빈 후처리기 적용 (Initialization / Proxying)</li>
     * </ol>
     *
     * @param clazz 생성할 빈의 클래스
     * @return 생성된 빈 인스턴스 (또는 프록시)
     */
    private Object createBean(Class<?> clazz) {
        String beanName = beanNameGenerator.determineBeanName(clazz);

        if (singletonObjects.containsKey(beanName)) {
            return singletonObjects.get(beanName);
        }

        try {
            Object instance = instantiateBean(clazz); // 1. 인스턴스화

            // 2. 조기 노출 (Early Exposure): 순환 참조가 발생했을 때, 의존성 주입 중인 미완성 객체라도 참조할 수 있게 함
            singletonObjects.put(beanName, instance);

            injectFields(instance); // 3. 의존성 주입

            // BeanPostProcessor 자체는 후처리 대상에서 제외 (무한 루프 방지)
            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                return instance;
            }

            Object exposedObject = applyBeanPostProcessors(instance, beanName); // 4. 후처리 (프록시 교체 등)

            // 후처리기가 객체를 프록시로 감싸거나 교체했다면 레지스트리 업데이트
            if (exposedObject != instance) {
                singletonObjects.put(beanName, exposedObject);
            }
            return exposedObject;

        } catch (Exception e) {
            // 생성 실패 시 불완전한 객체가 맵에 남지 않도록 정리
            singletonObjects.remove(beanName);
            throw new SpringException(ErrorMessage.BEAN_CREATION_FAILED);
        }
    }

    /**
     * 빈 생성 후, 등록된 {@link BeanPostProcessor}들을 순차적으로 적용합니다. AOP 프록시 적용이나 @PostConstruct 같은 초기화 작업이
     * 이곳에서 수행될 수 있습니다.
     */
    private Object applyBeanPostProcessors(Object existingBean, String beanName) throws Exception {
        Object result = existingBean;

        for (BeanPostProcessor processor : getBeanPostProcessors()) {
            Object current = processor.postProcess(result, beanName);
            // 후처리기가 null을 반환하면 이후 처리기를 무시하고 현재 객체 반환 (Spring 스펙 준수)
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }

    /**
     * 지연 로딩(Lazy Loading) 방식으로 BeanPostProcessor 목록을 조회합니다. 컨테이너에 등록된 빈 중 BeanPostProcessor 타입을 찾아
     * 리스트로 구성합니다.
     */
    private List<BeanPostProcessor> getBeanPostProcessors() throws Exception {
        if (!this.beanPostProcessors.isEmpty()) {
            return this.beanPostProcessors;
        }

        for (Class<?> clazz : componentClasses) {
            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                try {
                    // 후처리기 빈을 먼저 생성하여 활성화
                    BeanPostProcessor processor = (BeanPostProcessor) getBean(clazz);
                    this.beanPostProcessors.add(processor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return this.beanPostProcessors;
    }

    /**
     * 클래스 정보를 바탕으로 인스턴스를 생성합니다.
     *
     * <ul>
     * <li>인터페이스인 경우: @Repository가 붙어있다면 동적 프록시 생성</li>
     * <li>클래스인 경우: 생성자 주입(@Autowired) 또는 기본 생성자 사용</li>
     * </ul>
     */
    private Object instantiateBean(Class<?> clazz) throws Exception {
        // 1. 인터페이스 처리 (JPA 스타일의 Repository 자동 구현)
        if (clazz.isInterface()) {
            if (clazz.isAnnotationPresent(Repository.class)) {
                return createRepositoryProxy(clazz);
            }
            throw new SpringException(ErrorMessage.BEAN_CREATION_FAILED);
        }

        // 2. 생성자 결정 로직
        Constructor<?> targetConstructor = null;
        Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();

        // 생성자가 하나뿐이면 묵시적으로 의존성 주입 대상으로 간주
        if (declaredConstructors.length == 1) {
            targetConstructor = declaredConstructors[0];
        } else {
            // 여러 개라면 @Autowired가 붙은 생성자 탐색
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if (constructor.isAnnotationPresent(Autowired.class)) {
                    if (targetConstructor != null) {
                        throw new SpringException(ErrorMessage.NOT_UNIQUE_AUTOWIRED);
                    }
                    targetConstructor = constructor;
                }
            }
        }

        // 적절한 생성자가 없으면 기본 생성자 시도
        if (targetConstructor == null) {
            return clazz.getDeclaredConstructor().newInstance();
        }

        // 3. 생성자 파라미터 주입 (재귀적 빈 조회)
        Class<?>[] parameterTypes = targetConstructor.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            args[i] = getBean(parameterTypes[i]);
        }

        return targetConstructor.newInstance(args);
    }

    /**
     * JDK Dynamic Proxy를 사용하여 인터페이스 기반의 Repository 구현체를 런타임에 생성합니다. 실제 쿼리 실행은
     * {@link RepositoryHandler}에게 위임됩니다.
     */
    private Object createRepositoryProxy(Class<?> repositoryInterface) {
        JdbcTemplate jdbcTemplate = getBean(JdbcTemplate.class);
        Class<?> entityType = extractEntityType(repositoryInterface);
        RepositoryHandler handler = new RepositoryHandler(jdbcTemplate, entityType);

        return Proxy.newProxyInstance(
            repositoryInterface.getClassLoader(),
            new Class[]{repositoryInterface},
            handler
        );
    }

    /**
     * Repository 인터페이스의 제네릭 선언을 분석하여 엔티티 타입을 추출합니다. 예: interface UserRepository extends
     * JpaRepository<User, Long> -> User.class 반환
     */
    private Class<?> extractEntityType(Class<?> repositoryInterface) {
        Type[] interfaces = repositoryInterface.getGenericInterfaces();

        for (Type type : interfaces) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                // 관례적으로 첫 번째 제네릭 인자가 엔티티 타입이라고 가정
                Type typeArgument = pt.getActualTypeArguments()[0];
                if (typeArgument instanceof Class) {
                    return (Class<?>) typeArgument;
                }
            }
        }
        throw new SpringException(ErrorMessage.ENTITY_TYPE_NOT_FOUND);
    }

    /**
     * 필드에 붙은 @Autowired를 처리하여 의존성을 주입합니다. private 필드에도 접근 가능하도록 처리합니다.
     */
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

    /**
     * 주어진 클래스가 @Component 또는 그 메타 어노테이션을 가지고 있는지 판별합니다. 성능 최적화를 위해 결과를 캐싱합니다. * @param clazz 검사할
     * 클래스
     *
     * @return 컴포넌트 여부
     */
    public boolean hasComponentAnnotation(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) {
            return true;
        }

        for (Annotation annotation : clazz.getAnnotations()) {
            if (isComponent(annotation.annotationType())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 어노테이션 계층 구조를 재귀적으로 탐색하여 @Component의 존재 여부를 확인합니다.
     */
    private boolean isComponent(Class<? extends Annotation> annotationType) {
        if (annotationType.getPackageName().startsWith("java.lang") ||
            annotationType.getPackageName().startsWith("kotlin")) {
            return false;
        }

        if (componentAnnotationCache.containsKey(annotationType)) {
            return componentAnnotationCache.get(annotationType);
        }

        if (annotationType.isAnnotationPresent(Component.class)) {
            componentAnnotationCache.put(annotationType, true);
            return true;
        }

        for (Annotation metaAnnotation : annotationType.getAnnotations()) {
            if (isComponent(metaAnnotation.annotationType())) {
                componentAnnotationCache.put(annotationType, true);
                return true;
            }
        }

        componentAnnotationCache.put(annotationType, false);
        return false;
    }
}