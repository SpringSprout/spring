package com.spring.sprout.bundle.context;

import com.spring.sprout.bundle.api.Environment;
import com.spring.sprout.global.error.ErrorMessage;
import com.spring.sprout.global.error.SpringException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * [환경 설정 관리 구현체]
 *
 * <p>애플리케이션 실행 환경에 대한 설정값(Properties)을 로드하고 관리하는 표준 구현체입니다.
 * 클래스패스(Classpath) 상의 설정 파일을 읽어 키-값 쌍으로 제공하며, 애플리케이션의 설정 정보(DB 연결 정보, 포트 번호 등)를 코드와 분리하는 역할을
 * 합니다.</p>
 *
 * <p>기본 동작:</p>
 * <ul>
 * <li>기본 생성자 사용 시 'application.properties' 파일을 자동으로 로드합니다. (Convention over Configuration)</li>
 * <li>파일이 존재하지 않을 경우 오류를 발생시키지 않고 빈 설정 상태로 시작합니다 (Optional).</li>
 * <li>JAR 패키징 환경에서도 리소스를 올바르게 로드하기 위해 {@link ClassLoader}를 사용합니다.</li>
 * </ul>
 *
 * @see Environment
 * @see java.util.Properties
 */
public class EnvironmentImpl implements Environment {

    /**
     * 기본 설정 파일 경로
     */
    private static final String PROPERTIES_PATH = "application.properties";

    /**
     * 로드된 설정값들을 저장하는 내부 저장소
     */
    private final Properties properties = new Properties();

    /**
     * 기본 생성자.
     * <p>클래스패스 루트의 {@code application.properties} 파일에서 설정을 로드합니다.</p>
     */
    public EnvironmentImpl() {
        loadProperties(PROPERTIES_PATH);
    }

    /**
     * 사용자 정의 경로 생성자.
     * <p>테스트 환경이나 다중 환경 설정 시 특정 파일 경로를 지정할 수 있습니다.</p>
     *
     * @param configPath 로드할 설정 파일의 경로 (classpath 기준)
     */
    public EnvironmentImpl(String configPath) {
        loadProperties(configPath);
    }

    /**
     * 클래스패스 리소스로부터 프로퍼티 파일을 읽어 메모리에 적재합니다.
     *
     * @param fileName 읽어올 파일 이름
     * @throws SpringException 파일 읽기 중 I/O 오류가 발생한 경우 (단, 파일이 없는 경우는 무시)
     */
    private void loadProperties(String fileName) {
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
            // 설정 파일이 없으면 로딩을 건너뛰고 빈 상태 유지 (선택적 설정 지원)
            if (input == null) {
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new SpringException(ErrorMessage.FILE_NOT_LOADED);
        }
    }

    /**
     * 키에 해당하는 프로퍼티 값을 반환합니다.
     *
     * @param key 조회할 설정 키
     * @return 설정값 문자열, 또는 키가 존재하지 않을 경우 {@code null}
     */
    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}