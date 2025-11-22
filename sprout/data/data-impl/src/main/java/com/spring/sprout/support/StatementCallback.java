package com.spring.sprout.data.support;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementCallback<T> {

    T doInStatement(PreparedStatement ps) throws SQLException;
}