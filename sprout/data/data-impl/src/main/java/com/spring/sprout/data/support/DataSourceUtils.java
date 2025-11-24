package com.spring.sprout.data.support;

import com.spring.sprout.data.tranaction.TransactionSynchronizationManager;
import java.sql.Connection;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * [JDBC 커넥션 관리 헬퍼 클래스]
 *
 * <p>Spring의 트랜잭션 동기화 기능과 연동하여 JDBC Connection을 획득하고 해제하는 정적 유틸리티 메서드를 제공합니다.
 * 이 클래스는 애플리케이션 코드가 직접 JDBC Connection을 다루지 않게 하고, 트랜잭션 컨텍스트 내에서 동일한 Connection을 공유(Connection
 * Sharing)할 수 있도록 보장합니다.</p>
 *
 * <p>주요 역할:</p>
 * <ul>
 * <li><b>트랜잭션 인식:</b> 현재 스레드에 활성화된 트랜잭션이 있는지 확인합니다.</li>
 * <li><b>커넥션 재사용:</b> 트랜잭션 중이라면 이미 열린 커넥션을 반환하여 원자성(Atomicity)을 보장합니다.</li>
 * <li><b>스마트한 해제:</b> 트랜잭션이 끝나지 않았는데 커넥션을 닫으려 할 경우 이를 무시하고 유지합니다.</li>
 * </ul>
 *
 * @see TransactionSynchronizationManager
 * @see javax.sql.DataSource
 */
public class DataSourceUtils {

    /**
     * 주어진 DataSource에서 Connection을 획득합니다.
     *
     * <p>트랜잭션 동기화 매니저({@link TransactionSynchronizationManager})를 확인하여,
     * 현재 스레드에 이미 바인딩된 커넥션이 있다면 그것을 반환합니다 (트랜잭션 참여). 트랜잭션이 없는 경우, DataSource에서 새로운 커넥션을 가져와
     * 반환합니다.</p>
     *
     * @param dataSource 커넥션을 생성할 데이터소스
     * @return 트랜잭션에 바인딩된 커넥션 또는 새로운 커넥션
     * @throws SQLException 커넥션 획득 실패 시
     */
    public static Connection getConnection(DataSource dataSource) throws SQLException {
        // 1. 트랜잭션 동기화 매니저(ThreadLocal)에 보관된 커넥션이 있는지 확인
        Connection conn = TransactionSynchronizationManager.getResource();
        if (conn != null) {
            // 이미 존재한다는 것은 트랜잭션이 시작되었다는 의미이므로 해당 커넥션을 재사용
            return conn;
        }

        // 2. 트랜잭션 컨텍스트가 아니라면(일반 조회 등), 물리적으로 새로운 커넥션을 생성하여 반환
        return dataSource.getConnection();
    }

    /**
     * 사용이 끝난 Connection을 닫거나 유지합니다.
     *
     * <p>일반적인 JDBC 패턴에서는 {@code close()}를 즉시 호출하지만,
     * 트랜잭션 환경에서는 커밋/롤백이 일어날 때까지 커넥션이 열려 있어야 합니다. 따라서 이 메서드는 현재 커넥션이 트랜잭션에 묶여있는지 확인하고, 그렇다면 닫지
     * 않고(no-op) 유지시킵니다.</p>
     *
     * @param conn       닫을 대상 커넥션
     * @param dataSource 해당 커넥션을 생성한 데이터소스
     */
    public static void releaseConnection(Connection conn, DataSource dataSource) {
        try {
            // 1. 현재 트랜잭션 동기화 매니저가 관리 중인 커넥션인지 확인
            Connection holder = TransactionSynchronizationManager.getResource();

            // 관리 중인 커넥션과 동일하다면, 아직 트랜잭션이 끝나지 않았으므로 닫지 않음
            if (conn == holder) {
                return;
            }

            // 2. 트랜잭션과 무관한 커넥션(단순 조회 등)이라면 즉시 리소스 해제
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}