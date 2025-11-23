package com.spring.sprout.data.tranaction;

import com.spring.sprout.global.annotation.Autowired;
import com.spring.sprout.global.annotation.Component;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

@Component
public class TransactionManager {

    private final DataSource dataSource;

    @Autowired
    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // transaction 시작
    public void begin() {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false); // 오토 커밋 해제 -> 트랙잭션 시작
            TransactionSynchronizationManager.init(conn); // connection 트랜잭션에 보관
            System.out.println(">>> Transaction begin");
        } catch (SQLException e) {
            throw new RuntimeException("트랜잭션 시작 실패", e);
        }
    }

    // transaction 결과 commit
    public void commit() {
        Connection conn = TransactionSynchronizationManager.getResource();
        try {
            conn.commit(); // DB 반영
            System.out.println(">>> Transaction commit");
            processCleanup(conn);
        } catch (SQLException e) {
            throw new RuntimeException("커밋 실패", e);
        }
    }

    // transaction 실패 시 롤백
    public void rollback() {
        Connection conn = TransactionSynchronizationManager.getResource();
        try {
            conn.rollback(); // 되돌리기
            System.out.println(">>> Transaction rollback");
            processCleanup(conn);
        } catch (SQLException e) {
            throw new RuntimeException("롤백 실패", e);
        }
    }

    // 상태 정리
    private void processCleanup(Connection conn) throws SQLException {
        conn.setAutoCommit(true); // 상태 복구
        conn.close(); // 실제 커넥션 종료
        TransactionSynchronizationManager.clear(); // 스레드 비우기
    }
}
