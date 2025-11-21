package com.spring.sprout.data.support;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementCallback<T> {

    // sql문 입력 후 결과 반환
    T doInStatement(PreparedStatement ps) throws SQLException;
}