package com.spring.sprout.data.tranaction;

import com.spring.sprout.global.annotation.db.Transactional;
import java.lang.reflect.Method;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * [트랜잭션 AOP 인터셉터]
 *
 * <p>CGLIB 프록시의 메서드 호출을 가로채어(Intercept), 대상 메서드의 실행 전후에
 * 트랜잭션 경계(Transaction Boundary)를 설정하는 역할을 수행합니다.</p>
 *
 * <p>작동 흐름 (Around Advice):</p>
 * <ol>
 * <li>클라이언트가 프록시 객체의 메서드를 호출합니다.</li>
 * <li>이 인터셉터가 호출을 가로채고 {@link Transactional} 어노테이션 존재 여부를 확인합니다.</li>
 * <li><b>트랜잭션 대상인 경우:</b>
 * <ul>
 * <li>{@code transactionManager.begin()}을 호출하여 DB 트랜잭션을 시작합니다.</li>
 * <li>실제 비즈니스 로직({@code proxy.invokeSuper})을 실행합니다.</li>
 * <li>예외 없이 완료되면 {@code commit()}, 예외 발생 시 {@code rollback()}을 수행합니다.</li>
 * </ul>
 * </li>
 * <li><b>대상이 아닌 경우:</b> 트랜잭션 로직 없이 비즈니스 로직만 실행하고 빠져나갑니다.</li>
 * </ol>
 *
 * @see TransactionManager
 * @see net.sf.cglib.proxy.MethodInterceptor
 */
public class TransactionInterceptor implements MethodInterceptor {

    private final TransactionManager transactionManager;

    /**
     * 인터셉터 생성자. 실제 트랜잭션 제어(JDBC Connection 제어)를 담당할 매니저를 주입받습니다.
     *
     * @param transactionManager 트랜잭션 관리자
     */
    public TransactionInterceptor(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * CGLIB 프록시 메서드 실행 시 호출되는 콜백 메서드입니다.
     *
     * @param obj    프록시 객체 자신
     * @param method 호출된 메서드 정보 (Reflection)
     * @param args   메서드 인자 목록
     * @param proxy  부모 클래스(원본)의 메서드를 호출하기 위한 프록시 객체
     * @return 메서드 실행 결과
     * @throws Throwable 실행 중 발생한 모든 예외
     */
    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
        throws Throwable {
        // 1. @Transactional 적용 여부 확인 (메서드 레벨 우선, 그 다음 클래스 레벨)
        boolean isTransactional = method.isAnnotationPresent(Transactional.class)
            || method.getDeclaringClass().isAnnotationPresent(Transactional.class);

        // 트랜잭션 대상이 아니라면, 부가 기능 없이 원본 로직만 실행하고 종료
        if (!isTransactional) {
            return proxy.invokeSuper(obj, args);
        }

        // 2. 트랜잭션 경계 설정 (Try-Catch-Finally 블록과 유사)
        try {
            // 트랜잭션 시작 (AutoCommit false 설정 및 Connection 동기화)
            transactionManager.begin();

            // 실제 비즈니스 로직 실행 (CGLIB은 invokeSuper를 통해 원본 로직을 수행함)
            Object result = proxy.invokeSuper(obj, args);

            // 로직이 성공적으로 완료되면 변경사항 확정
            transactionManager.commit();
            return result;

        } catch (Exception e) {
            // 예외 발생 시 변경사항 취소
            transactionManager.rollback();
            throw e; // 예외를 먹어버리지 않고 상위로 던져서 컨트롤러가 알게 함
        }
    }
}