package com.spring.sprout.bundle.api;

/**
 * [실행 환경에 대한 추상화 인터페이스]
 *
 * <p>애플리케이션이 실행되는 환경(로컬, 개발, 운영 등)에 따라 달라질 수 있는
 * 설정값(Properties)을 조회하는 통합된 창구를 제공합니다.</p>
 *
 * <p>이 인터페이스를 사용함으로써 비즈니스 로직은 설정 정보가
 * 파일(.properties)에서 왔는지, 환경 변수(System Env)에서 왔는지 알 필요 없이 키(Key)만으로 값을 조회할 수 있습니다 (환경의 캡슐화).</p>
 */
public interface Environment {

    /**
     * 지정된 키에 연관된 프로퍼티 값을 반환합니다.
     *
     * @param key 조회할 설정 키 (예: "server.port", "db.url")
     * @return 설정된 값 (존재하지 않을 경우 null 반환 가능성 있음)
     */
    String getProperty(String key);
}