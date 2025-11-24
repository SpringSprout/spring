package com.spring.sprout.data.support;

import com.spring.sprout.data.tranaction.TransactionSynchronizationManager;
import java.sql.Connection;
import javax.sql.DataSource;
import java.sql.SQLException;

public class DataSourceUtils {

    // 커넥션 생성
    public static Connection getConnection(DataSource dataSource) throws SQLException {
        // 1. 트랜잭션 동기화 매니저에 보관된 커넥션 있는지 확인
        Connection conn = TransactionSynchronizationManager.getResource();
        if (conn != null) {
            return conn; // 이미 존재하면 트랜잭션 반환
        }

        // 2. 없으면 즉, 트랜잭션이 아니면 새로 생성해서 반환
        return dataSource.getConnection();
    }

    // 커넥션 해재
    public static void releaseConnection(Connection conn, DataSource dataSource) {
        try {
            // 1. 트랜잭션 중인 커넥션이면 닫지 않고 유지
            Connection holder = TransactionSynchronizationManager.getResource();
            if (conn == holder) {
                return;
            }

            // 2. 트랜잭션이 아니면 바로 닫음
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
