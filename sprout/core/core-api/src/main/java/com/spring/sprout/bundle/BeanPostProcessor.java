package com.spring.sprout.bundle;

/**
 * [빈 후처리기 (Bean Post Processor) 인터페이스]
 *
 * <p>컨테이너의 핵심 확장 포인트(Extension Point)입니다.
 * 컨테이너가 빈을 생성하고 의존성 주입을 마친 후, 초기화(Initialization) 단계 전후에 개발자가 커스텀 로직을 개입시킬 수 있는 훅(Hook)을
 * 제공합니다.</p>
 *
 * <p>주요 용도:</p>
 * <ul>
 * <li>빈 인스턴스의 속성 검사 및 수정</li>
 * <li>프록시 객체로의 래핑 (AOP, 트랜잭션 처리 등)</li>
 * <li>초기화 메서드(@PostConstruct) 실행 전후 처리</li>
 * </ul>
 */
public interface BeanPostProcessor {

    /**
     * 빈 초기화(Initialization) 이후에 호출되어, 빈 인스턴스를 변형하거나 교체할 기회를 제공합니다.
     *
     * <p>반환값은 원본 빈일 수도 있고, 프록시로 감싸진 래퍼(Wrapper) 객체일 수도 있습니다.
     * 만약 null을 반환하면 이후의 후처리기 체인은 중단될 수 있습니다.</p>
     *
     * @param bean     원본 빈 인스턴스
     * @param beanName 빈의 이름
     * @return 후처리된 빈 인스턴스 (기본 구현은 원본 그대로 반환)
     */
    default Object postProcess(Object bean, String beanName) {
        return bean;
    }
}