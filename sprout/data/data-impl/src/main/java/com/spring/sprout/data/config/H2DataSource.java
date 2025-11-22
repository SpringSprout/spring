package com.spring.sprout.data.config;

import com.spring.sprout.core.api.Environment;
import com.spring.sprout.global.annotation.Autowired;
import com.spring.sprout.global.annotation.Component;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

@Component
public class H2DataSource implements DataSource {

    private final JdbcDataSource h2DataSource;

    @Autowired
    public H2DataSource(Environment env) {
        String url = env.getProperty("db.url");
        String user = env.getProperty("db.username");
        String password = env.getProperty("db.password");

        this.h2DataSource = new JdbcDataSource();
        this.h2DataSource.setURL(url);
        this.h2DataSource.setUser(user);
        this.h2DataSource.setPassword(password);

        System.out.println("H2 Database Connected to: " + url);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return h2DataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return h2DataSource.getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return h2DataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        h2DataSource.setLogWriter(out);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return h2DataSource.getLoginTimeout();
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        h2DataSource.setLoginTimeout(seconds);
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return h2DataSource.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return h2DataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return h2DataSource.isWrapperFor(iface);
    }
}
