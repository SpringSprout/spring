package com.spring.sprout.data.config;

import com.spring.sprout.bundle.api.Environment;
import com.spring.sprout.global.annotation.Autowired;
import com.spring.sprout.global.annotation.Component;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

@Component
public class SimpleDataSource implements DataSource {

    private final String url;
    private final String username;
    private final String password;
    private final String driverClassName;

    @Autowired
    public SimpleDataSource(Environment env) {
        this.url = env.getProperty("db.url");
        this.username = env.getProperty("db.username");
        this.password = env.getProperty("db.password");
        this.driverClassName = env.getProperty("db.driver-class-name");

        try {
            Class.forName(driverClassName);
            System.out.println("Database Connected to: " + url);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("DB Driver 로딩 실패: " + driverClassName, e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public PrintWriter getLogWriter() {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
    }

    @Override
    public void setLoginTimeout(int seconds) {
    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
