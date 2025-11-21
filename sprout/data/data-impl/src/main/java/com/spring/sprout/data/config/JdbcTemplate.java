package com.spring.sprout.data.config;

import com.spring.sprout.annotation.Autowired;
import com.spring.sprout.annotation.Component;
import com.spring.sprout.data.support.StatementCallback;
import com.spring.sprout.error.ErrorMessage;
import com.spring.sprout.error.SpringException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

@Component
public class JdbcTemplate {

    private final DataSource dataSource;

    @Autowired
    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // sql문과 콜백 실행
    public <T> T execute(String sql, StatementCallback<T> callback) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            // 1. 연결, SQL문 받기
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            // 2. 콜백 실행
            return callback.doInStatement(preparedStatement);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new SpringException(ErrorMessage.SQL_EXECUTION_FILED);
        } finally {
            close(preparedStatement, connection);
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

