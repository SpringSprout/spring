package com.spring.sprout.data.support;

import com.spring.sprout.bundle.BeanPostProcessor;
import com.spring.sprout.data.tranaction.TransactionInterceptor;
import com.spring.sprout.data.tranaction.TransactionManager;
import com.spring.sprout.global.annotation.Autowired;
import com.spring.sprout.global.annotation.Component;
import com.spring.sprout.global.annotation.Transactional;
import java.lang.reflect.Method;
import net.sf.cglib.proxy.Enhancer;

// 역할: 빈 생성 후 @Transaction 대상이면 proxy 교체
@Component
public class TransactionBeanPostProcessor implements BeanPostProcessor {

    private final TransactionManager transactionManager;

    @Autowired
    public TransactionBeanPostProcessor(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    // proxy 객체 반환
    @Override
    public Object postProcess(Object bean, String beanName) {
        Class<?> clazz = bean.getClass();

        // @Transactional이 있는지 확인
        if (!hasTransactionalAnnotation(clazz)) {
            return bean; // 대상 아니면 원본 반환
        }
        return createProxy(bean, clazz); // 맞으면 proxy 만들어서 반환
    }

    // 프록시 생성
    private Object createProxy(Object originalBean, Class<?> clazz) {
        // 트랜잭션 로직 수행 인터셉터
        TransactionInterceptor interceptor = new TransactionInterceptor(transactionManager);

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz); // 원본 클래스 상속
        enhancer.setCallback(interceptor); // 메서드 호출 시 callBack 인터셉터 설정

        Object proxy = enhancer.create();
        copyFields(originalBean, proxy, clazz); // 프록시 객체 생성
        return proxy;
    }

    // @Transactional 확인 메서드
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

    // 필드 복사
    private void copyFields(Object source, Object target, Class<?> clazz) {
        try {
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true); // private 필드 접근 허용
                Object value = field.get(source); // 원본에서 꺼내서
                field.set(target, value);         // 프록시에 넣는다
            }
        } catch (Exception e) {
            throw new RuntimeException("프록시 필드 복사 실패", e);
        }
    }

}
