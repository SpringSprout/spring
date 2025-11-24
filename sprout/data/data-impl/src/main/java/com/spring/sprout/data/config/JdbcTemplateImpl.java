package com.spring.sprout.data.config;

import com.spring.sprout.JdbcTemplate;
import com.spring.sprout.data.support.DataSourceUtils;
import com.spring.sprout.global.annotation.Autowired;
import com.spring.sprout.global.annotation.Component;
import com.spring.sprout.data.support.EntityMapper;
import com.spring.sprout.StatementCallback;
import com.spring.sprout.global.error.ErrorMessage;
import com.spring.sprout.global.error.SpringException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/**
 * [JDBC 템플릿의 표준 구현체]
 *
 * <p>JDBC API를 사용하여 데이터베이스와 상호작용하는 핵심 클래스입니다.
 * 템플릿 콜백 패턴을 적용하여 반복적인 리소스 관리 코드를 제거하고, 개발자가 SQL 로직에만 집중할 수 있는 환경을 제공합니다.</p>
 *
 * <p>주요 기능:</p>
 * <ul>
 * <li><b>자원 관리:</b> Connection 획득 및 반환, Statement 닫기 등을 안전하게 처리 (try-catch-finally)</li>
 * <li><b>예외 변환:</b> 체크드 예외인 {@link SQLException}을 런타임 예외인 {@link SpringException}으로 변환</li>
 * <li><b>객체 매핑:</b> {@link EntityMapper}를 통해 ResultSet을 자바 객체로 자동 변환</li>
 * </ul>
 *
 * @see JdbcTemplate
 * @see DataSourceUtils
 */
@Component
public class JdbcTemplateImpl implements JdbcTemplate {

    private final DataSource dataSource;

    /**
     * 데이터소스(커넥션 풀)를 주입받아 템플릿 인스턴스를 생성합니다.
     *
     * @param dataSource DB 연결을 제공할 데이터소스
     */
    @Autowired
    public JdbcTemplateImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * SELECT 쿼리를 실행하고 결과를 객체 리스트로 매핑하여 반환합니다.
     *
     * <p>{@link EntityMapper}를 사용하여 ResultSet의 각 행(Row)을
     * 지정된 클래스 타입의 객체로 변환합니다.</p>
     *
     * @param sql   실행할 SQL 쿼리
     * @param clazz 결과 매핑 대상 클래스
     * @param args  바인딩할 파라미터들
     * @return 매핑된 객체 리스트 (결과가 없으면 빈 리스트)
     */
    @Override
    public <T> List<T> query(String sql, Class<T> clazz, Object... args) {
        EntityMapper<T> mapper = new EntityMapper<>(clazz);

        // execute 메서드에 ResultSet 처리를 위임
        return execute(sql, ps -> {
            List<T> results = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.mapRow(rs));
                }
            }
            return results;
        }, args);
    }

    /**
     * 템플릿 콜백 패턴의 핵심 실행 메서드입니다.
     *
     * <p>JDBC 작업의 표준 흐름(Context)을 제어하며, 실제 SQL 실행 로직은
     * {@link StatementCallback}에게 위임합니다.</p>
     *
     * <p>실행 흐름:</p>
     * <ol>
     * <li>Connection 획득 (트랜잭션 동기화 지원을 위해 {@link DataSourceUtils} 사용)</li>
     * <li>PreparedStatement 생성 및 파라미터 바인딩</li>
     * <li>Callback 실행 (SQL 수행)</li>
     * <li>리소스 정리 및 예외 처리</li>
     * </ol>
     *
     * @param sql      실행할 SQL
     * @param callback 실행할 콜백 로직 (람다)
     * @param args     바인딩 파라미터
     * @return 콜백의 실행 결과
     * @throws SpringException SQL 실행 중 오류 발생 시
     */
    @Override
    public <T> T execute(String sql, StatementCallback<T> callback, Object... args) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            // 1. 트랜잭션 매니저와 연동 가능한 커넥션 획득
            connection = DataSourceUtils.getConnection(dataSource);
            preparedStatement = connection.prepareStatement(sql);

            // 2. 파라미터 바인딩
            setParameters(preparedStatement, args);

            // 3. 콜백 실행 (비즈니스 로직 수행)
            return callback.doInStatement(preparedStatement);

        } catch (SQLException e) {
            e.printStackTrace(); // 로깅으로 대체 권장
            throw new SpringException(ErrorMessage.SQL_EXECUTION_FILED);
        } finally {
            closePreparedStatement(preparedStatement);
            // 커넥션은 닫지 않고 DataSourceUtils에게 반환 (트랜잭션 유지 등을 위해)
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    /**
     * PreparedStatement 리소스를 안전하게 해제합니다. 예외가 발생하더라도 무시하고 로그만 남깁니다.
     */
    private void closePreparedStatement(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (Exception e) {
                // 리소스 해제 중 오류는 흐름에 영향을 주지 않도록 무시
            }
        }
    }

    /**
     * SQL의 '?' 플레이스홀더에 파라미터를 바인딩합니다. 인덱스는 1부터 시작합니다.
     *
     * @param ps   준비된 Statement
     * @param args 바인딩할 값들
     * @throws SQLException 바인딩 실패 시
     */
    private void setParameters(PreparedStatement ps, Object... args) throws SQLException {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                // JDBC는 1-based index 사용
                ps.setObject(i + 1, args[i]);
            }
        }
    }
}