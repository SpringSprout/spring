package com.spring.sprout.data.tranaction;

import com.spring.sprout.global.annotation.Autowired;
import com.spring.sprout.global.annotation.Component;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * [트랜잭션 관리자 (Platform Transaction Manager)]
 *
 * <p>물리적인 JDBC Connection의 생명주기를 제어하여 트랜잭션 경계(Boundary)를 설정합니다.
 * AOP 인터셉터의 요청을 받아 실제 데이터베이스와의 트랜잭션 동기화를 수행합니다.</p>
 *
 * <p>핵심 기능:</p>
 * <ul>
 * <li><b>트랜잭션 시작 (Begin):</b> AutoCommit을 비활성화하고, 커넥션을 스레드 로컬에 동기화합니다.</li>
 * <li><b>커밋 (Commit):</b> 작업이 성공하면 변경 사항을 DB에 영구 반영합니다.</li>
 * <li><b>롤백 (Rollback):</b> 예외 발생 시 모든 작업을 취소하고 이전 상태로 되돌립니다.</li>
 * <li><b>리소스 정리 (Cleanup):</b> 커넥션 설정을 원복하고 스레드 로컬을 비웁니다.</li>
 * </ul>
 *
 * @see TransactionSynchronizationManager
 * @see javax.sql.DataSource
 */
@Component
public class TransactionManager {

    private final DataSource dataSource;

    @Autowired
    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 새로운 트랜잭션을 시작합니다.
     *
     * <p>작동 과정:</p>
     * <ol>
     * <li>DataSource로부터 새로운 Connection을 획득합니다.</li>
     * <li>JDBC의 <b>AutoCommit 모드를 false로 설정</b>하여 수동 커밋 모드로 전환합니다. (트랜잭션 시작의 핵심)</li>
     * <li>동기화 매니저({@link TransactionSynchronizationManager})에 커넥션을 등록하여,
     * 이후의 로직(Repository 등)이 동일한 커넥션을 사용하도록 보장합니다.</li>
     * </ol>
     */
    public void begin() {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false); // 오토 커밋 해제 -> 트랜잭션의 실질적 시작
            TransactionSynchronizationManager.init(conn); // 획득한 커넥션을 현재 스레드에 바인딩
            System.out.println(">>> Transaction begin");
        } catch (SQLException e) {
            throw new RuntimeException("트랜잭션 시작 실패", e);
        }
    }

    /**
     * 현재 트랜잭션의 모든 변경 사항을 데이터베이스에 확정(Commit)합니다. 정상적으로 로직이 수행되었을 때 호출됩니다.
     */
    public void commit() {
        Connection conn = TransactionSynchronizationManager.getResource();
        try {
            conn.commit(); // DB 반영
            System.out.println(">>> Transaction commit");
            processCleanup(conn); // 리소스 정리
        } catch (SQLException e) {
            throw new RuntimeException("커밋 실패", e);
        }
    }

    /**
     * 작업 중 예외가 발생했을 때, 트랜잭션 시작 이후의 모든 변경 사항을 취소(Rollback)합니다. 데이터의 일관성(Consistency)을 유지하기 위한
     * 안전장치입니다.
     */
    public void rollback() {
        Connection conn = TransactionSynchronizationManager.getResource();
        try {
            if (conn != null) {
                conn.rollback(); // 되돌리기
                System.out.println(">>> Transaction rollback");
                processCleanup(conn); // 리소스 정리
            }
        } catch (SQLException e) {
            throw new RuntimeException("롤백 실패", e);
        }
    }

    /**
     * 트랜잭션 종료 후 리소스를 정리하고 상태를 복구합니다.
     *
     * <p>정리 작업:</p>
     * <ul>
     * <li><b>AutoCommit 복구:</b> 커넥션을 풀(Pool)로 돌려보내기 전에 기본값(true)으로 복구합니다.</li>
     * <li><b>커넥션 종료:</b> 물리적 연결을 닫거나 풀에 반환합니다.</li>
     * <li><b>동기화 해제:</b> 스레드 로컬에 저장된 커넥션 정보를 제거하여 메모리 누수를 방지합니다.</li>
     * </ul>
     */
    private void processCleanup(Connection conn) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(true); // 다음 사용자를 위해 상태 복구 (Pooling 환경에서 필수)
            conn.close(); // Connection Pool로 반환
        }
        TransactionSynchronizationManager.clear(); // 스레드 컨텍스트 정리
    }
}