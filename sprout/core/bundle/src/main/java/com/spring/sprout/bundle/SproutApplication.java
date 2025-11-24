package com.spring.sprout.bundle;

import com.spring.sprout.bundle.context.EnvironmentImpl;
import com.spring.sprout.bundle.context.SproutApplicationContext;
import com.spring.sprout.bundle.io.ResourcePatternResolver;
import com.spring.sprout.web.api.WebServer;

/**
 * [Sprout 프레임워크 애플리케이션 부트스트래퍼]
 *
 * <p>애플리케이션의 진입점(Entry Point) 역할을 수행하며, 프레임워크의 구동에 필요한 모든 인프라를
 * 순차적으로 초기화하고 조립합니다. Spring Boot의 {@code SpringApplication}과 유사한 역할을 하며, 복잡한 컨테이너 설정 과정을 단일 정적 메서드로
 * 추상화하여 제공합니다.</p>
 *
 * <p>구동 프로세스:</p>
 * <ol>
 * <li><b>배너 출력:</b> 애플리케이션 시작을 알리는 로고 출력</li>
 * <li><b>환경 설정 로드:</b> {@link EnvironmentImpl}을 통해 프로퍼티 파일 로드</li>
 * <li><b>컨텍스트 생성:</b> {@link SproutApplicationContext} 및 내부 스캐너 초기화</li>
 * <li><b>핵심 빈 등록:</b> BeanFactory 자신과 Environment를 빈으로 등록 (Self-Registration)</li>
 * <li><b>컴포넌트 스캔:</b> 프레임워크 내부 패키지 및 사용자 애플리케이션 패키지 스캔</li>
 * <li><b>컨텍스트 리프레시:</b> 빈 인스턴스화 및 의존성 주입 완료</li>
 * <li><b>웹 서버 구동:</b> 내장 웹 서버({@link WebServer}) 실행 및 요청 대기 시작</li>
 * </ol>
 */
public class SproutApplication {

    /**
     * 프레임워크 내부 컴포넌트(내장 기능)들이 위치한 기본 패키지 경로
     */
    private static final String CONFIG_BASE_PACKAGE = "com.spring.sprout";

    /**
     * Sprout 애플리케이션을 실행합니다.
     *
     * <p>메인 클래스의 위치를 기반으로 베이스 패키지를 추론하거나 설정 파일에서 읽어와
     * 컴포넌트 스캔을 수행하고, 웹 서버를 띄워 서비스를 시작합니다.</p>
     *
     * @param mainClass 애플리케이션의 메인 클래스 (패키지 탐색의 기준점)
     * @return 초기화가 완료된 애플리케이션 컨텍스트
     * @throws RuntimeException 초기화 과정 중 예외 발생 시 래핑하여 던짐
     */
    public static SproutApplicationContext run(Class<?> mainClass) {
        printBanner();

        // 1. 인프라 준비
        EnvironmentImpl environment = new EnvironmentImpl();
        ResourcePatternResolver scanner = new ResourcePatternResolver();

        // 2. 컨텍스트 생성 및 기초 의존성 주입
        SproutApplicationContext context = new SproutApplicationContext(scanner);
        // 컨텍스트 자신과 환경 설정 객체도 빈으로 주입받을 수 있도록 등록
        context.registerSingleton("beanFactory", context);
        context.registerSingleton("environment", environment);

        try {
            // 3. 스캔 대상 패키지 결정 (설정 파일 우선, 없으면 메인 클래스 패키지 사용)
            String basePackage = environment.getProperty("scan.base-package");

            if (basePackage == null || basePackage.isBlank()) {
                basePackage = mainClass.getPackageName();
            }

            // 4. 컴포넌트 스캔 (사용자 패키지 + 프레임워크 내부 패키지)
            context.scan(basePackage);
            context.scan(CONFIG_BASE_PACKAGE);

            // 5. 빈 생명주기 실행 (객체 생성 -> 의존성 주입 -> 초기화)
            context.refresh();

            // 6. 웹 서버 시작
            startWebServer(context);

            // 7. 진단 정보 출력
            printAllThreads();
            System.out.println("Spring Application Started Successfully");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Spring Application Start Failed", e);
        }

        return context;
    }

    /**
     * 컨텍스트에서 WebServer 빈을 찾아 실행하고, JVM 종료 시점에 대한 훅을 등록합니다.
     *
     * <p><b>Graceful Shutdown:</b>
     * 프로세스가 종료 신호(SIGTERM 등)를 받으면, 즉시 종료되지 않고 웹 서버의 {@code stop()} 메서드를 호출하여 리소스를 정리할 시간을
     * 확보합니다.</p>
     */
    private static void startWebServer(SproutApplicationContext context) {
        // 컨테이너가 관리하는 WebServer 구현체(Tomcat, Jetty 등)를 조회
        WebServer webServer = context.getBean(WebServer.class);
        webServer.init();
        webServer.start();

        // JVM 종료 시 콜백 등록
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println(">> Shutdown Signal Detected. Stopping WebServer...");
            webServer.stop();
            System.out.println(">> WebServer Stopped Successfully.");
        }));
    }

    /**
     * 현재 실행 중인 모든 스레드의 상태를 출력합니다. (디버깅 목적)
     */
    private static void printAllThreads() {
        Thread.getAllStackTraces().keySet().forEach(thread ->
            System.out.println(
                "Thread: " + thread.getName() + " (Daemon: " + thread.isDaemon() + ")")
        );
    }

    private static void printBanner() {
        System.out.println("""
              _____                  _
             / ____|                 | |
            | (___  _ __  _ __ ___  _| |_
            \\___ \\| '_ \\| '__/ _ \\| | __|
             ____) | |_) | | | (_) | | |_
            |_____/| .__/|_|  \\___/ \\__|
                   | |
                   |_|
            
             :: Sprout ::             (v1.0.0)
            """);
    }
}