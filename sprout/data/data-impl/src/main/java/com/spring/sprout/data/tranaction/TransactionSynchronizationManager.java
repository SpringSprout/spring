package com.spring.sprout.data.tranaction;

import java.sql.Connection;

public class TransactionSynchronizationManager {

    private static final ThreadLocal<Connection> resources = new ThreadLocal<>();

    // 트랜잭션 시작 시 커낵션 보관
    public static void init(Connection conn) {
        if (resources.get() != null) {
            System.out.println("트랜잭션 진행중입니다.");
        }
        resources.set(conn);
    }

    // 커넥션 꺼내기
    public static Connection getResource() {
        return resources.get();
    }

    // 트랜잭션 종료 시, 커넥션 제거
    public static void clear() {
        resources.remove();
    }
}
