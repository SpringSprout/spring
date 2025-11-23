package com.spring.sprout.web;

import java.util.Objects;

/**
 * [핸들러 매핑을 위한 식별자 키 객체]
 *
 * <p>HTTP 요청을 특정 컨트롤러 메서드와 매핑하기 위해 사용하는 복합 키(Composite Key)입니다.
 * 단순히 URL 경로뿐만 아니라 HTTP 메서드(GET, POST 등)까지 조합하여 요청의 유일성을 보장합니다.</p>
 *
 * <p>이 클래스는 {@link DispatcherServlet} 내부의 {@code HashMap}에서 Key로 사용되므로,
 * 논리적 동등성 비교를 위한 {@code equals}와 {@code hashCode}가 필수적으로 구현되어 있습니다.</p>
 *
 * <p>예시:</p>
 * <ul>
 * <li>Key 1: {url: "/users", method: GET} -> 회원 목록 조회 핸들러</li>
 * <li>Key 2: {url: "/users", method: POST} -> 회원 가입 핸들러</li>
 * </ul>
 *
 * @see DispatcherServlet
 * @see java.util.HashMap
 */
public class HandlerKey {

    private final String url;
    private final RequestMethod requestMethod;

    /**
     * 핸들러 키를 생성합니다. 불변(Immutable) 객체로 설계되어 생성 후에는 상태가 변경되지 않습니다.
     *
     * @param url           요청 URL 경로 (예: "/api/hello")
     * @param requestMethod HTTP 요청 메서드 (예: GET, POST)
     */
    public HandlerKey(String url, RequestMethod requestMethod) {
        this.url = url;
        this.requestMethod = requestMethod;
    }

    public String getUrl() {
        return url;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    /**
     * 두 HandlerKey 객체의 논리적 동등성을 비교합니다. URL과 Request Method가 모두 일치해야 같은 키로 간주합니다.
     *
     * @param o 비교할 대상 객체
     * @return 두 객체의 내용이 같으면 true
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HandlerKey that = (HandlerKey) o;
        return Objects.equals(url, that.url) && requestMethod == that.requestMethod;
    }

    /**
     * 객체의 해시 코드를 생성합니다. HashMap 등 해시 기반 컬렉션에서 성능 저하(해시 충돌)를 막고 올바르게 동작하기 위해 필수입니다.
     *
     * @return url과 requestMethod를 기반으로 생성된 해시값
     */
    @Override
    public int hashCode() {
        return Objects.hash(url, requestMethod);
    }
}