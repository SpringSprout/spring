package com.spring.sprout;

import java.util.List;

/**
 * [JDBC 작업을 위한 핵심 템플릿 인터페이스]
 *
 * <p>JDBC API 사용 시 발생하는 반복적인 작업(Connection 획득/반환, Statement 생성, 예외 처리 등)을
 * 내부적으로 처리하고, 개발자는 핵심 SQL과 파라미터 설정에만 집중할 수 있도록 돕습니다.</p>
 *
 * <p>주요 특징:</p>
 * <ul>
 * <li><b>리소스 관리 자동화:</b> try-catch-finally 블록을 통한 리소스 해제를 템플릿 내부에서 보장합니다.</li>
 * <li><b>객체 매핑:</b> ResultSet의 결과를 자바 객체(POJO)로 자동 매핑하는 기능을 제공합니다.</li>
 * <li><b>유연성:</b> 콜백 인터페이스를 통해 저수준의 JDBC 작업도 수행할 수 있습니다.</li>
 * </ul>
 */
public interface JdbcTemplate {

    /**
     * SQL 쿼리를 실행하고 결과를 객체 리스트로 매핑하여 반환합니다.
     *
     * @param sql   실행할 SQL 쿼리 (PreparedStatement 구문 사용 가능, 예: "SELECT * FROM users WHERE id = ?")
     * @param clazz 결과 로우(Row)를 매핑할 대상 클래스 타입
     * @param args  SQL 바인딩 파라미터 (가변 인자)
     * @param <T>   매핑할 객체의 제네릭 타입
     * @return 매핑된 객체 리스트 (결과가 없으면 빈 리스트 반환)
     */
    <T> List<T> query(String sql, Class<T> clazz, Object... args);

    /**
     * 임의의 SQL 작업을 수행하기 위한 일반화된 메서드입니다. PreparedStatement를 직접 제어해야 하는 복잡한 로직이나 업데이트 작업에 사용됩니다.
     *
     * @param sql      실행할 SQL 쿼리
     * @param callback PreparedStatement를 전달받아 실행 로직을 정의하는 콜백
     * @param args     SQL 바인딩 파라미터
     * @param <T>      반환할 결과 타입
     * @return 콜백 실행 결과
     */
    <T> T execute(String sql, StatementCallback<T> callback, Object... args);
}