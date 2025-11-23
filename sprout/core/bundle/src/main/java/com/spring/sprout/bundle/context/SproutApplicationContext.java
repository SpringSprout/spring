package com.spring.sprout.bundle.context;

import com.spring.sprout.bundle.beanfactory.DefaultBeanFactory;
import com.spring.sprout.bundle.io.Resource;
import com.spring.sprout.bundle.io.ResourcePatternResolver;
import com.spring.sprout.global.error.ErrorMessage;
import com.spring.sprout.global.error.SpringException;

/**
 * [애플리케이션 컨텍스트의 구현체]
 *
 * <p>IoC 컨테이너의 핵심 인터페이스로, 단순한 빈 관리 기능({@link DefaultBeanFactory})에 더해
 * 자동 컴포넌트 스캔 및 애플리케이션 생명주기 제어 기능을 제공합니다.</p>
 *
 * <p>주요 역할:</p>
 * <ul>
 * <li><b>컴포넌트 스캔 (Component Scan):</b> 지정된 패키지 하위의 클래스들을 탐색하여 자동으로 빈으로 등록합니다.</li>
 * <li><b>리소스 로딩 (Resource Loading):</b> 파일 시스템이나 클래스패스에서 리소스를 읽어오는 기능을 위임받아 수행합니다.</li>
 * <li><b>컨텍스트 초기화 (Refresh):</b> 등록된 모든 빈을 인스턴스화하고 초기화하는 진입점 역할을 합니다.</li>
 * </ul>
 *
 * @see DefaultBeanFactory
 * @see ResourcePatternResolver
 */
public class SproutApplicationContext extends DefaultBeanFactory {

    /**
     * 리소스 검색 및 경로 해석을 담당하는 전략 인터페이스
     */
    private final ResourcePatternResolver scanner;

    /**
     * 리소스 패턴 리졸버를 주입받아 컨텍스트를 생성합니다.
     *
     * @param scanner 파일 시스템이나 클래스패스 탐색을 담당할 리졸버
     */
    public SproutApplicationContext(ResourcePatternResolver scanner) {
        this.scanner = scanner;
    }

    /**
     * 지정된 베이스 패키지 하위의 모든 클래스를 탐색하여 컴포넌트를 등록합니다.
     *
     * <p>작동 과정:</p>
     * <ol>
     * <li>ResourcePatternResolver를 통해 물리적인 .class 파일들을 찾습니다.</li>
     * <li>파일 경로를 점(.)으로 구분된 완전한 클래스 이름(FQCN)으로 변환합니다.</li>
     * <li>클래스로 로딩 후, @Component 어노테이션(메타 포함) 존재 여부를 확인합니다.</li>
     * <li>유효한 컴포넌트라면 부모 팩토리({@code super})에 등록합니다.</li>
     * </ol>
     *
     * @param basePackage 탐색을 시작할 최상위 패키지 경로 (예: "com.example.app")
     * @throws SpringException 클래스 로딩 실패 혹은 스캔 중 오류 발생 시
     */
    public void scan(String basePackage) {
        Resource[] resources = scanner.getResources(basePackage);
        for (Resource resource : resources) {
            try {
                String className = convertPathToClassName(resource.getPath());
                Class<?> clazz = getClassLoader().loadClass(className);

                // 어노테이션 자체는 빈으로 등록하지 않음
                if (clazz.isAnnotation()) {
                    continue;
                }

                // @Component가 붙은 클래스만 선별하여 등록
                if (hasComponentAnnotation(clazz)) {
                    super.registerBeanClass(clazz);
                }
            } catch (Exception e) {
                throw new SpringException(ErrorMessage.BEAN_SCAN_FAILED);
            }
        }
    }

    /**
     * 컨텍스트를 새로고침(Refresh)하여 애플리케이션을 시작합니다.
     *
     * <p>이 시점에 등록된 모든 싱글톤 빈들이 실제로 생성(Eager Initialization)되며,
     * 의존성 주입이 완료됩니다. 애플리케이션이 요청을 처리할 준비를 마치는 단계입니다.</p>
     */
    public void refresh() {
        super.preInstantiateSingletons();
    }

    /**
     * 파일 시스템 경로(슬래시 구분)를 자바 클래스 이름(점 구분)으로 변환합니다. 예: "com/example/MyClass.class" ->
     * "com.example.MyClass"
     *
     * @param path 리소스 경로
     * @return 변환된 클래스 이름
     */
    private String convertPathToClassName(String path) {
        return path.replace(".class", "").replace("/", ".");
    }

    /**
     * 현재 스캐너가 사용 중인 클래스 로더를 반환합니다.
     */
    public ClassLoader getClassLoader() {
        return this.scanner.getClassLoader();
    }
}