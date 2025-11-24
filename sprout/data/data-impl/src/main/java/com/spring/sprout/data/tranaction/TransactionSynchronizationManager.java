package com.spring.sprout.data.tranaction;

import java.sql.Connection;

/**
 * [트랜잭션 리소스 동기화 매니저]
 *
 * <p>스레드 로컬(ThreadLocal)을 사용하여, 데이터베이스 커넥션과 같은 트랜잭션 리소스를
 * 현재 실행 중인 스레드에 바인딩(Binding)하고 관리하는 저장소입니다.</p>
 *
 * <p>핵심 역할:</p>
 * <ul>
 * <li><b>스레드 격리 (Thread Confinement):</b> 멀티스레드 환경에서 각 요청(스레드)이 서로의 커넥션을 침범하지 못하도록 격리합니다.</li>
 * <li><b>파라미터 제거:</b> Connection 객체를 메서드 인자로 계속 전달하지 않아도, 어디서든 접근 가능하게 합니다.</li>
 * <li><b>트랜잭션 전파 지원:</b> 하나의 트랜잭션 내에서 여러 DAO가 실행될 때, 동일한 Connection을 사용하도록 보장합니다.</li>
 * </ul>
 *
 * @see java.lang.ThreadLocal
 * @see com.spring.sprout.data.tranaction.TransactionManager
 * @see com.spring.sprout.data.support.DataSourceUtils
 */
public class TransactionSynchronizationManager {

    /**
     * 스레드별로 고유하게 할당된 JDBC Connection을 저장하는 컨테이너입니다. ThreadLocal을 사용하므로 동기화(synchronized) 키워드 없이도
     * 스레드 안전(Thread-safe)합니다.
     */
    private static final ThreadLocal<Connection> resources = new ThreadLocal<>();

    /**
     * 트랜잭션이 시작될 때, 생성된 커넥션을 현재 스레드에 보관합니다.
     *
     * <p>이미 진행 중인 트랜잭션이 있다면(리소스가 존재한다면),
     * 중첩 트랜잭션이나 로직 오류일 수 있으므로 경고를 출력하거나 예외를 던져야 합니다.</p>
     *
     * @param conn 트랜잭션에 사용될 활성화된(AutoCommit=false) 커넥션
     */
    public static void init(Connection conn) {
        if (resources.get() != null) {
            System.out.println("⚠️ 경고: 이미 활성화된 트랜잭션이 존재합니다. (중첩 호출 가능성)");
        }
        resources.set(conn);
    }

    /**
     * 현재 스레드에 바인딩된 커넥션을 조회합니다.
     *
     * @return 현재 트랜잭션 중인 커넥션 객체, 트랜잭션이 없다면 null 반환
     */
    public static Connection getResource() {
        return resources.get();
    }

    /**
     * 트랜잭션이 종료(커밋/롤백)된 후, 스레드에 보관된 리소스를 제거합니다.
     *
     * <p><b>매우 중요:</b> 스레드 풀(Thread Pool) 환경에서는 스레드가 재사용되므로,
     * 사용 후 반드시 값을 제거해야만 다음 요청에서 오염된(Dirty) 커넥션을 사용하는 것을 방지할 수 있습니다. (메모리 누수 방지)</p>
     */
    public static void clear() {
        resources.remove();
    }
}