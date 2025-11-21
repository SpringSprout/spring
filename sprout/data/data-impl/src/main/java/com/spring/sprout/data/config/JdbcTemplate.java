package com.spring.sprout.data.config;

import com.spring.sprout.annotation.Autowired;
import com.spring.sprout.annotation.Component;
import com.spring.sprout.data.support.EntityMapper;
import com.spring.sprout.data.support.StatementCallback;
import com.spring.sprout.error.ErrorMessage;
import com.spring.sprout.error.SpringException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

@Component
public class JdbcTemplate {

    private final DataSource dataSource;

    @Autowired
    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // 쿼리문 실행
    public <T> List<T> query(String sql, Class<T> clazz, Object... args) {
        EntityMapper<T> mapper = new EntityMapper<>(clazz);

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

    // sql문과 콜백 실행
    public <T> T execute(String sql, StatementCallback<T> callback, Object... args) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            // 1. 연결, SQL문 받기
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            // 2. 파라미터 바인딩 추가
            setParameters(preparedStatement, args);

            // 3. 콜백 실행
            return callback.doInStatement(preparedStatement);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new SpringException(ErrorMessage.SQL_EXECUTION_FILED);
        } finally {
            close(preparedStatement, connection);
        }
    }

    // 파라미터 바인딩
    private void setParameters(PreparedStatement ps, Object... args) throws SQLException {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
        }
    }

    // 자원 해제
    private void close(PreparedStatement preparedStatement, Connection connection) {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }
}

