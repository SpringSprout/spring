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

/**
 * [기본 JDBC 데이터소스 구현체]
 *
 * <p>표준 JDBC {@link DriverManager}를 기반으로 하는 단순한 {@link DataSource} 구현체입니다.
 * {@link Environment} 설정을 통해 DB 접속 정보를 로드하며, 요청 시마다 새로운 물리적 연결(Connection)을 생성합니다.</p>
 *
 * <p><b>주의:</b> 이 구현체는 커넥션 풀(Connection Pool) 기능을 제공하지 않습니다.
 * 따라서 트래픽이 많은 상용 환경보다는 개발, 테스트, 또는 단순한 프레임워크 학습 용도로 사용하기에 적합합니다.</p>
 *
 * <p>필요한 설정 키 (application.properties):</p>
 * <ul>
 * <li>{@code db.driver-class-name}: JDBC 드라이버 클래스명</li>
 * <li>{@code db.url}: 데이터베이스 접속 URL</li>
 * <li>{@code db.username}: 계정 아이디</li>
 * <li>{@code db.password}: 계정 비밀번호</li>
 * </ul>
 *
 * @see javax.sql.DataSource
 * @see java.sql.DriverManager
 */
@Component
public class SimpleDataSource implements DataSource {

    private final String url;
    private final String username;
    private final String password;
    private final String driverClassName;

    /**
     * 환경 설정 객체로부터 DB 접속 정보를 읽어와 초기화합니다. 생성 시점에 JDBC 드라이버 클래스를 로딩하여 연결 준비를 마칩니다.
     *
     * @param env 애플리케이션 환경 설정 (프로퍼티 값 제공)
     * @throws RuntimeException 지정된 JDBC 드라이버 클래스를 찾을 수 없는 경우
     */
    @Autowired
    public SimpleDataSource(Environment env) {
        this.url = env.getProperty("db.url");
        this.username = env.getProperty("db.username");
        this.password = env.getProperty("db.password");
        this.driverClassName = env.getProperty("db.driver-class-name");

        try {
            // JDBC 드라이버 로딩 (초기화 검증)
            Class.forName(driverClassName);
            System.out.println("Database Connected to: " + url);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("DB Driver 로딩 실패: " + driverClassName, e);
        }
    }

    /**
     * 설정된 접속 정보를 사용하여 새로운 데이터베이스 연결을 생성합니다.
     *
     * <p>커넥션 풀을 사용하지 않으므로 호출 시마다 매번 3-way handshake를 포함한
     * 물리적인 연결 과정이 수행됩니다.</p>
     *
     * @return 새로운 Connection 객체
     * @throws SQLException DB 접속 실패 시
     */
    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * 사용자 이름과 비밀번호를 명시적으로 지정하여 연결을 생성합니다. (이 구현체에서는 주로 기본 설정값을 사용하므로 잘 사용되지 않습니다.)
     */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    // --- DataSource 인터페이스의 미사용 메서드 (구현 생략) ---

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