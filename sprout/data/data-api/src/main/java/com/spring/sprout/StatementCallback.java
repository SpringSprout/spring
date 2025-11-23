package com.spring.sprout;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * [Statement 실행 콜백 인터페이스]
 *
 * <p>{@link JdbcTemplate} 내부에서 JDBC {@link PreparedStatement}를 다루는 로직을
 * 함수형 인터페이스로 주입받기 위해 사용됩니다.</p>
 *
 * <p>템플릿 콜백 패턴(Template Callback Pattern)의 '콜백(Callback)' 역할을 수행하며,
 * 람다 표현식을 통해 쿼리 실행, 결과 추출 등의 구체적인 동작을 정의합니다.</p>
 *
 * @param <T> 콜백 실행 후 반환할 결과 타입
 */
@FunctionalInterface
public interface StatementCallback<T> {

    /**
     * 준비된 PreparedStatement를 사용하여 실제 DB 작업을 수행합니다.
     *
     * @param ps 파라미터가 바인딩된 PreparedStatement 객체
     * @return SQL 실행 결과
     * @throws SQLException JDBC 작업 중 발생하는 예외 (템플릿에서 처리됨)
     */
    T doInStatement(PreparedStatement ps) throws SQLException;
}