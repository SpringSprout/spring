package com.spring.sprout.bundle.beanfactory;

import java.util.Map;

/**
 * [IoC 컨테이너의 최상위 루트 인터페이스]
 *
 * <p>Spring 프레임워크 IoC(Inversion of Control) 컨테이너의 기본 기능을 정의합니다.
 * 빈(Bean)의 생성, 조회, 의존성 관리 등 컨테이너로서 갖춰야 할 최소한의 기능을 명시합니다.</p>
 *
 * <p>이 인터페이스는 'Service Locator' 패턴의 형태를 띠고 있지만,
 * 실제로는 의존성 주입(DI)을 통해 내부적으로 사용되는 것이 권장됩니다.</p>
 */
public interface BeanFactory {

    /**
     * 이름(ID)으로 빈 인스턴스를 조회합니다.
     *
     * @param name 빈의 등록 이름
     * @return 관리되는 빈 인스턴스 (Object 타입이므로 캐스팅 필요)
     */
    Object getBean(String name);

    /**
     * 타입(Class)으로 빈 인스턴스를 조회합니다. (Type-safe)
     *
     * @param requiredType 조회할 빈의 타입
     * @param <T>          반환할 제네릭 타입
     * @return 해당 타입의 빈 인스턴스
     */
    <T> T getBean(Class<T> requiredType);

    /**
     * 특정 타입(또는 그 하위 타입)에 해당하는 모든 빈을 조회합니다.
     *
     * @param type 조회할 빈의 타입
     * @param <T>  빈의 제네릭 타입
     * @return 빈 이름과 인스턴스를 매핑한 Map (없을 경우 빈 Map 반환)
     */
    <T> Map<String, T> getBeansOfType(Class<T> type);

    /**
     * 컨테이너에 등록된 모든 빈을 조회합니다.
     * <p>주로 프레임워크 내부의 디버깅이나 모니터링 목적으로 사용됩니다.</p>
     *
     * @return 모든 빈의 이름과 인스턴스 Map
     */
    Map<String, Object> getAllBeans();
}