package com.spring.sprout.data.support;

import com.spring.sprout.bundle.BeanPostProcessor;
import com.spring.sprout.data.tranaction.TransactionInterceptor;
import com.spring.sprout.data.tranaction.TransactionManager;
import com.spring.sprout.global.annotation.Autowired;
import com.spring.sprout.global.annotation.Component;
import com.spring.sprout.global.annotation.db.Transactional;
import java.lang.reflect.Method;
import net.sf.cglib.proxy.Enhancer;

/**
 * [트랜잭션 AOP 프록시 생성 후처리기]
 *
 * <p>컨테이너 초기화 단계에서 빈(Bean)을 검사하여 트랜잭션 적용 대상인지 확인하고,
 * 대상일 경우 CGLIB을 사용하여 트랜잭션 기능이 추가된 프록시 객체로 교체(Wrapping)합니다.</p>
 *
 * <p>작동 원리:</p>
 * <ol>
 * <li>빈 생성 후 {@code postProcess} 메서드가 호출됩니다.</li>
 * <li>클래스나 메서드에 {@link Transactional} 어노테이션이 있는지 확인합니다.</li>
 * <li>있다면 CGLIB {@link Enhancer}를 사용하여 원본 클래스를 상속받는 프록시를 생성합니다.</li>
 * <li>프록시 내부의 메서드 호출을 {@link TransactionInterceptor}로 가로채어 트랜잭션을 제어합니다.</li>
 * <li>원본 빈의 필드 상태(의존성 주입된 값 등)를 프록시 객체로 복사하여 상태를 동기화합니다.</li>
 * </ol>
 *
 * @see BeanPostProcessor
 * @see TransactionInterceptor
 * @see net.sf.cglib.proxy.Enhancer
 */
@Component
public class TransactionBeanPostProcessor implements BeanPostProcessor {

    private final TransactionManager transactionManager;

    @Autowired
    public TransactionBeanPostProcessor(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * 빈 초기화 후킹 메서드입니다. 트랜잭션 어노테이션 존재 여부를 확인하여 프록시 교체 여부를 결정합니다.
     *
     * @param bean     원본 빈 인스턴스
     * @param beanName 빈의 이름
     * @return 트랜잭션 대상이면 프록시 객체, 아니면 원본 객체
     */
    @Override
    public Object postProcess(Object bean, String beanName) {
        Class<?> clazz = bean.getClass();

        // @Transactional 마킹 확인 (클래스 레벨 or 메서드 레벨)
        if (!hasTransactionalAnnotation(clazz)) {
            return bean; // 대상이 아니면 원본 그대로 반환
        }

        // 대상이라면 프록시를 생성하여 반환 (컨테이너에는 이 프록시가 등록됨)
        return createProxy(bean, clazz);
    }

    /**
     * CGLIB을 사용하여 원본 클래스를 상속받는 동적 프록시(Dynamic Proxy)를 생성합니다.
     *
     * @param originalBean 상태를 복사해올 원본 객체
     * @param clazz        원본 클래스 타입
     * @return 생성된 프록시 객체
     */
    private Object createProxy(Object originalBean, Class<?> clazz) {
        // 실제 트랜잭션 begin/commit/rollback을 수행할 인터셉터
        TransactionInterceptor interceptor = new TransactionInterceptor(transactionManager);

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz); // CGLIB은 상속을 통해 프록시를 구현함
        enhancer.setCallback(interceptor); // 모든 메서드 호출을 인터셉터로 연결

        Object proxy = enhancer.create(); // 프록시 인스턴스 생성

        // 중요: CGLIB 프록시는 새로운 객체이므로, 원본 빈에 주입된 의존성(Repository 등)을 복사해야 함
        copyFields(originalBean, proxy, clazz);

        return proxy;
    }

    /**
     * 클래스 또는 메서드 레벨에 @Transactional이 붙어있는지 재귀적으로 검사합니다.
     */
    private boolean hasTransactionalAnnotation(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Transactional.class)) {
            return true;
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Transactional.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 원본 객체의 필드 값들을 프록시 객체로 얕은 복사(Shallow Copy)합니다. 원본 객체에 이미 주입된 @Autowired 필드들을 프록시에서도 사용할 수 있게
     * 하기 위함입니다.
     */
    private void copyFields(Object source, Object target, Class<?> clazz) {
        try {
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true); // private 필드 접근 허용
                Object value = field.get(source); // 원본에서 값 추출
                field.set(target, value);         // 프록시에 주입
            }
        } catch (Exception e) {
            throw new RuntimeException("프록시 필드 복사 실패: " + clazz.getName(), e);
        }
    }
}