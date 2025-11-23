package com.spring.sprout.web.api;

/**
 * [내장 웹 서버 추상화 인터페이스]
 *
 * <p>애플리케이션 내에서 실행되는 웹 서버(Tomcat, Jetty, Netty 등)의
 * 생명주기(Lifecycle)를 제어하는 공통 규약을 정의합니다.</p>
 *
 * <p>이 인터페이스를 통해 프레임워크는 하부의 구체적인 서버 기술에 의존하지 않고,
 * 일관된 방식으로 서버를 초기화, 시작, 종료할 수 있습니다. (Portable Service Abstraction)</p>
 *
 * <p>일반적인 실행 순서:</p>
 * <pre>
 * WebServer server = context.getBean(WebServer.class);
 * server.init();  // 설정 로드 및 커넥터 준비
 * server.start(); // 포트 바인딩 및 요청 대기 시작
 * ...
 * server.stop();  // 리소스 정리 및 종료
 * </pre>
 * * @see com.spring.sprout.bundle.SproutApplication
 */
public interface WebServer {

    /**
     * 웹 서버 구동을 위한 사전 준비 작업을 수행합니다.
     *
     * <p>주요 작업:</p>
     * <ul>
     * <li>서블릿 컨테이너(Servlet Container) 생성 및 설정</li>
     * <li>필터(Filter) 및 서블릿(DispatcherServlet) 등록</li>
     * <li>포트 번호 등 환경 설정 값 로드</li>
     * </ul>
     * * @throws SpringException 초기화 중 설정 오류 발생 시
     */
    void init();

    /**
     * 서버를 시작하고 지정된 포트로 들어오는 클라이언트의 요청을 대기(Listen)합니다.
     *
     * <p>이 메서드가 호출되는 순간부터 외부와의 통신이 가능해지며,
     * 메인 스레드를 차단(Blocking)하거나 별도의 데몬 스레드에서 서버를 구동시킵니다.</p> * @throws SpringException 포트 충돌(Bind
     * Exception) 또는 서버 시작 실패 시
     */
    void start();

    /**
     * 실행 중인 서버를 우아하게 종료(Graceful Shutdown)합니다.
     *
     * <p>주요 작업:</p>
     * <ul>
     * <li>새로운 요청의 수락 중단</li>
     * <li>처리 중인 요청이 완료될 때까지 대기 (선택적 구현)</li>
     * <li>소켓 연결 해제 및 스레드 풀 리소스 반환</li>
     * </ul>
     */
    void stop();
}
