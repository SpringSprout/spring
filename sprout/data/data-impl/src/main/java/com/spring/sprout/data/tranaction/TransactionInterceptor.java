package com.spring.sprout.data.tranaction;

import com.spring.sprout.global.annotation.Transactional;
import java.lang.reflect.Method;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class TransactionInterceptor implements MethodInterceptor {

    private final TransactionManager transactionManager;

    public TransactionInterceptor(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
        throws Throwable {
        // 1. @Transactional 어노테이션 확인
        boolean isTransactional = method.isAnnotationPresent(Transactional.class)
            || method.getDeclaringClass().isAnnotationPresent(Transactional.class);

        if (!isTransactional) { // 없으면 그냥 메서드 실행
            return proxy.invokeSuper(obj, args);
        }

        // 2. 트랜잭션 처리 로직
        try {
            transactionManager.begin(); // 시작
            Object result = proxy.invokeSuper(obj, args); //  메서드 실행
            transactionManager.commit(); // 성공 시 커밋
            return result;
        } catch (Exception e) {
            transactionManager.rollback(); // 실패 시 롤백
            throw e;
        }
    }
}
