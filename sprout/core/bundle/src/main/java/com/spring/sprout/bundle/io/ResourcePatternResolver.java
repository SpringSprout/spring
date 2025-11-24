package com.spring.sprout.bundle.io;

import com.spring.sprout.global.annotation.Component;
import com.spring.sprout.global.error.ErrorMessage;
import com.spring.sprout.global.error.SpringException;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * [리소스 경로 패턴 해석기]
 *
 * <p>패키지 경로(예: "com.example.app")와 같은 논리적인 패턴을 해석하여,
 * 실제 물리적인 {@link Resource} 객체들의 집합으로 변환하는 전략 구현체입니다.</p>
 *
 * <p>이 클래스는 애플리케이션의 런타임 환경에 따라 리소스를 찾는 방식을 동적으로 결정합니다:</p>
 * <ul>
 * <li><b>파일 시스템(File Protocol):</b> 개발 환경(IDE)이나 압축이 풀린 디렉토리에서는 재귀적으로 폴더를 탐색합니다.</li>
 * <li><b>JAR 파일(Jar Protocol):</b> 빌드된 JAR 패키지 내부에서는 {@link JarURLConnection}을 통해 압축된 엔트리를 순회합니다.</li>
 * </ul>
 *
 * <p>주로 {@link com.spring.sprout.bundle.context.SproutApplicationContext}가 컴포넌트 스캔을 수행할 때,
 * 스캔 대상이 되는 .class 파일들을 수집하는 용도로 사용됩니다.</p>
 *
 * @see Resource
 * @see java.net.URL
 * @see java.util.jar.JarFile
 */
@Component
public class ResourcePatternResolver {

    private static final String CLASSPATH_URL_PREFIX = "classpath:";

    /**
     * 리소스 탐색에 사용할 클래스 로더 (Context ClassLoader 사용)
     */
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    /**
     * 단일 위치 경로를 해석하여 하나의 리소스 객체를 반환합니다. "classpath:" 접두어가 있다면 제거하고 처리합니다.
     *
     * @param location 리소스 위치 문자열
     * @return 리소스 객체
     */
    public Resource getResource(String location) {
        if (location.startsWith(CLASSPATH_URL_PREFIX)) {
            location = location.substring(CLASSPATH_URL_PREFIX.length());
        }
        return new Resource(location, this.classLoader);
    }

    /**
     * 주어진 위치 패턴(패키지 경로)에 매칭되는 모든 리소스를 검색합니다. 점(.)으로 구분된 패키지 경로를 슬래시(/)로 변환하여 탐색합니다.
     *
     * @param locationPattern 탐색할 패키지 경로 (예: "com.spring.sprout")
     * @return 발견된 모든 리소스 배열
     * @throws SpringException I/O 오류 발생 시
     */
    public Resource[] getResources(String locationPattern) {
        Set<Resource> result = new HashSet<>();

        // 패키지 구분자(.)를 디렉토리 구분자(/)로 변환
        String path = locationPattern.replace('.', '/');

        try {
            doFindResources(path, result);
        } catch (IOException e) {
            throw new SpringException(ErrorMessage.FILE_NOT_RESOLVED);
        }

        return result.toArray(new Resource[0]);
    }

    /**
     * 실제 리소스 탐색을 수행하는 메서드입니다. URL 프로토콜(file, jar)을 확인하여 적절한 탐색 전략을 분기합니다.
     */
    private void doFindResources(String path, Set<Resource> result) throws IOException {
        // 클래스패스 상에서 해당 경로를 가진 모든 리소스를 조회 (중복 포함)
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resourceUrl = resources.nextElement();
            String protocol = resourceUrl.getProtocol();

            // 1. 일반 파일 시스템 디렉토리인 경우
            if ("file".equals(protocol)) {
                try {
                    File directory = new File(resourceUrl.toURI());
                    findClassResourcesInDirectory(path, directory, result);
                } catch (java.net.URISyntaxException e) {
                    throw new IOException(e);
                }
            }
            // 2. JAR 파일 내부에 존재하는 경우
            else if ("jar".equals(protocol)) {
                findClassResourcesInJar(path, resourceUrl, result);
            }
        }
    }

    /**
     * 파일 시스템 디렉토리에서 재귀적으로 .class 파일을 탐색합니다.
     *
     * @param basePackagePath 기준 패키지 경로
     * @param directory       현재 탐색 중인 디렉토리
     * @param result          결과를 담을 Set
     */
    private void findClassResourcesInDirectory(String basePackagePath, File directory,
        Set<Resource> result) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            String fileName = file.getName();
            if (file.isDirectory()) {
                // 하위 디렉토리로 재귀 호출
                String subPackagePath = basePackagePath + "/" + fileName;
                findClassResourcesInDirectory(subPackagePath, file, result);
            } else if (fileName.endsWith(".class")) {
                // 클래스 파일 발견 시 리소스로 변환하여 추가
                String resourcePath = basePackagePath + "/" + fileName;
                result.add(new Resource(resourcePath, this.classLoader));
            }
        }
    }

    /**
     * JAR 파일 내부에서 주어진 경로로 시작하는 .class 파일을 탐색합니다. JAR는 디렉토리 구조가 아닌 Flat한 엔트리 목록이므로 스트림 필터링을 사용합니다.
     *
     * @param path        탐색할 기준 경로
     * @param resourceUrl JAR 파일의 URL
     * @param result      결과를 담을 Set
     */
    private void findClassResourcesInJar(String path, URL resourceUrl, Set<Resource> result)
        throws IOException {
        URLConnection con = resourceUrl.openConnection();

        if (!(con instanceof JarURLConnection)) {
            return;
        }

        JarURLConnection jarCon = (JarURLConnection) con;
        // JAR 파일 잠금 이슈 방지를 위해 캐시 사용 안함
        jarCon.setUseCaches(false);

        try (JarFile jarFile = jarCon.getJarFile()) {
            Collections.list(jarFile.entries()).stream()
                .map(JarEntry::getName)
                // 경로가 일치하고 .class 확장자를 가진 엔트리만 필터링
                .filter(name -> name.startsWith(path) && name.endsWith(".class"))
                .map(name -> new Resource(name, this.classLoader))
                .forEach(result::add);
        }
    }
}