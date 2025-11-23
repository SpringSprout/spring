package com.spring.sprout.web;

import com.spring.sprout.global.annotation.Component;
import com.spring.sprout.web.api.WebServer;
import java.io.File;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.AbstractProtocol;


/**
 * [임베디드 톰캣 웹 서버 구현체]
 *
 * <p>Apache Tomcat을 내장(Embedded)하여 애플리케이션 실행 시 웹 서버를 구동시키는 클래스입니다.
 * 별도의 WAS 설치 없이 자바 애플리케이션을 독립적으로 실행할 수 있게 해주며, HTTP 요청을 받아 {@link DispatcherServlet}으로 전달하는 진입점 역할을
 * 합니다.</p>
 *
 * <p>주요 설정:</p>
 * <ul>
 * <li><b>포트 설정:</b> 기본 8080 포트를 사용하여 리스닝합니다.</li>
 * <li><b>서블릿 등록:</b> {@link DispatcherServlet}을 루트 컨텍스트("/")에 매핑하여 모든 요청을 처리하게 합니다.</li>
 * <li><b>스레드 풀 튜닝:</b> 동시 접속 처리를 위해 MaxThreads, MinSpareThreads, AcceptCount 등을 직접 제어합니다.</li>
 * </ul>
 *
 * @see WebServer
 * @see org.apache.catalina.startup.Tomcat
 */
@Component
public class TomcatWebServer implements WebServer {

    private final int PORT = 8080;

    private final Tomcat tomcat;
    private final DispatcherServlet dispatcherServlet;

    /**
     * 톰캣 인스턴스를 생성하고, 요청 처리를 위임할 디스패처 서블릿을 주입받습니다.
     *
     * @param dispatcherServlet 모든 HTTP 요청을 처리할 프론트 컨트롤러
     */
    public TomcatWebServer(DispatcherServlet dispatcherServlet) {
        this.tomcat = new Tomcat();
        this.dispatcherServlet = dispatcherServlet;
    }

    /**
     * 톰캣 서버의 초기 설정을 수행합니다.
     *
     * <p>설정 상세:</p>
     * <ul>
     * <li><b>커넥터 설정:</b> NIO 프로토콜 핸들러를 가져와 스레드 풀 정책을 설정합니다.</li>
     * <li><b>스레드 정책:</b>
     * <ul>
     * <li>MaxThreads(50): 동시에 처리 가능한 최대 요청 수</li>
     * <li>MinSpareThreads(10): 유휴 상태일 때도 유지할 최소 스레드 수 (빠른 응답 보장)</li>
     * <li>AcceptCount(100): 작업 큐가 꽉 찼을 때 대기열(Backlog)의 크기</li>
     * </ul>
     * </li>
     * <li><b>컨텍스트 및 서블릿:</b> 임시 작업 디렉토리를 기반으로 Context를 생성하고, 디스패처 서블릿을 등록합니다.</li>
     * </ul>
     */
    @Override
    public void init() {
        // 서블릿 내부의 핸들러 매핑 초기화
        dispatcherServlet.init();

        tomcat.setPort(PORT);
        Connector connector = tomcat.getConnector();
        AbstractProtocol<?> protocol = (AbstractProtocol<?>) connector.getProtocolHandler();

        // 동시성 제어를 위한 스레드 풀 튜닝
        protocol.setMaxThreads(50);      // 최대 활성 스레드
        protocol.setMinSpareThreads(10); // 최소 유휴 스레드
        protocol.setAcceptCount(100);    // OS 레벨의 연결 대기 큐 크기

        // 톰캣 컨텍스트 생성 (docBase는 현재 디렉토리로 설정)
        Context context = tomcat.addContext("", new File(".").getAbsolutePath());

        // 서블릿 등록 및 URL 매핑 (모든 요청 "/" -> dispatcher)
        Tomcat.addServlet(context, "dispatcher", dispatcherServlet);
        context.addServletMappingDecoded("/", "dispatcher");
    }

    /**
     * 설정된 톰캣 서버를 시작합니다. 이 메서드 호출 후 서버는 클라이언트의 연결을 수락(Listening)하기 시작합니다.
     *
     * @throws RuntimeException 톰캣 시작 중 생명주기 예외 발생 시
     */
    @Override
    public void start() {
        try {
            tomcat.start();
            // 참고: 임베디드 톰캣은 start() 후 메인 스레드가 종료되지 않도록 await()가 필요할 수 있음
            // tomcat.getServer().await();
        } catch (LifecycleException e) {
            throw new RuntimeException("Tomcat Server Start Failed", e);
        }
    }

    /**
     * 실행 중인 톰캣 서버를 중지하고 리소스를 해제합니다. 애플리케이션 종료 시(Shutdown Hook 등) 호출되어야 합니다.
     */
    @Override
    public void stop() {
        try {
            tomcat.stop();
            tomcat.destroy();
        } catch (LifecycleException e) {
            throw new RuntimeException("Tomcat Server Stop Failed", e);
        }
    }
}