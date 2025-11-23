package com.spring.sprout;

import java.util.List;

public interface JdbcTemplate {

    <T> List<T> query(String sql, Class<T> clazz, Object... args);

    <T> T execute(String sql, StatementCallback<T> callback, Object... args);
}
