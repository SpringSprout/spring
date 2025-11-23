package com.spring.sprout.bundle.io;

import com.spring.sprout.global.error.ErrorMessage;
import com.spring.sprout.global.error.SpringException;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * [외부 리소스 추상화 객체]
 *
 * <p>파일 시스템이나 클래스패스(Classpath)에 존재하는 저수준(Low-level) 리소스에 대한 접근을 추상화합니다.
 * 물리적인 경로를 직접 다루는 대신, 클래스 로더를 통해 리소스를 로드하므로 애플리케이션의 배포 환경(로컬, 서버 등)에 구애받지 않는 이식성을 제공합니다.</p>
 *
 * <p>주요 특징:</p>
 * <ul>
 * <li><b>클래스패스 기반:</b> 주어진 경로를 클래스패스 루트 기준으로 해석합니다.</li>
 * <li><b>스트림 접근 권장:</b> JAR 패키징 환경을 고려하여 {@link #getInputStream()} 사용을 권장합니다.</li>
 * <li><b>파일 시스템 접근 제한:</b> {@link #getFile()}은 리소스가 실제 파일 시스템에 존재할 때만 작동합니다.</li>
 * </ul>
 *
 * @see java.lang.ClassLoader
 * @see java.net.URL
 */
public class Resource {

    /**
     * 리소스의 클래스패스 경로
     */
    private final String path;

    /**
     * 리소스를 탐색할 클래스 로더
     */
    private final ClassLoader classLoader;

    /**
     * 현재 스레드의 컨텍스트 클래스 로더(Context ClassLoader)를 사용하여 리소스 객체를 생성합니다. 웹 애플리케이션 등 컨테이너 환경에서 가장 안전한 로딩
     * 방식입니다.
     *
     * @param path 리소스 경로 (예: "application.properties")
     */
    public Resource(String path) {
        this.path = path;
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    /**
     * 특정 클래스 로더를 지정하여 리소스 객체를 생성합니다.
     *
     * @param path        리소스 경로
     * @param classLoader 사용할 클래스 로더
     */
    public Resource(String path, ClassLoader classLoader) {
        this.path = path;
        this.classLoader = classLoader;
    }

    /**
     * 리소스의 경로 문자열을 반환합니다.
     */
    public String getPath() {
        return path;
    }

    /**
     * 해당 리소스가 실제로 존재하는지 확인합니다.
     *
     * @return 리소스 존재 시 true, 아니면 false
     */
    public boolean exists() {
        return classLoader.getResource(this.path) != null;
    }

    /**
     * 리소스의 내용을 읽을 수 있는 입력 스트림을 반환합니다.
     * <p>이 메서드는 리소스가 JAR 파일 내부에 있어도 정상적으로 동작합니다.</p>
     *
     * @return 리소스에 연결된 InputStream (사용 후 닫아야 함)
     * @throws SpringException 리소스를 찾을 수 없는 경우
     */
    public InputStream getInputStream() {
        InputStream is = classLoader.getResourceAsStream(this.path);
        if (is == null) {
            throw new SpringException(ErrorMessage.FILE_NOT_FOUND);
        }
        return is;
    }

    /**
     * 리소스를 {@link java.io.File} 객체로 반환합니다.
     *
     * <p><b>주의:</b> 이 메서드는 리소스가 파일 시스템 상에 물리적으로 존재할 때만 동작합니다.
     * 만약 리소스가 JAR 파일 내부에 압축되어 있다면 URL 프로토콜이 'jar'가 되므로, {@link SpringException} (FILE_NOT_RESOLVED)
     * 예외가 발생합니다.</p>
     *
     * @return 파일 시스템 상의 File 객체
     * @throws SpringException 리소스를 찾을 수 없거나, 파일 시스템 경로로 변환할 수 없는 경우(JAR 내부 등)
     */
    public File getFile() {
        URL url = classLoader.getResource(this.path);
        if (url == null) {
            throw new SpringException(ErrorMessage.FILE_NOT_FOUND);
        }
        // URL이 파일 프로토콜(file://)이 아닌 경우 (예: jar:file:...) 접근 불가 처리
        if (!"file".equals(url.getProtocol())) {
            throw new SpringException(ErrorMessage.FILE_NOT_RESOLVED);
        }
        try {
            // URL을 URI로 변환하여 파일 생성 (공백이나 특수문자 처리 포함)
            return new File(url.toURI());
        } catch (java.net.URISyntaxException e) {
            // URI 변환 실패 시 레거시 방식으로 파일 생성
            return new File(url.getFile());
        }
    }
}