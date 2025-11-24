package com.spring.sprout;

import java.util.List;

/**
 * [JPA 스타일의 리포지토리 인터페이스]
 *
 * <p>도메인 엔티티에 대한 CRUD(Create, Read, Update, Delete) 작업을 추상화한 인터페이스입니다.
 * 개발자가 SQL을 직접 작성하지 않고도 기본적인 데이터 접근이 가능하도록 메서드를 정의합니다.</p>
 *
 * <p>이 인터페이스를 상속받은 인터페이스는 런타임에 프레임워크에 의해 자동으로
 * 구현체(JDK Dynamic Proxy)가 생성되어 빈으로 등록됩니다.</p>
 *
 * @param <T>  관리할 엔티티 타입
 * @param <ID> 엔티티 식별자(PK)의 타입
 */
public interface JpaRepository<T, ID> {

    /**
     * 엔티티를 데이터베이스에 저장하거나 업데이트합니다.
     *
     * @param entity 저장할 엔티티 객체
     */
    void save(T entity);

    /**
     * 식별자(ID)를 사용하여 엔티티를 조회합니다.
     *
     * @param id 조회할 엔티티의 기본 키
     * @return 조회된 엔티티 객체 (존재하지 않을 경우 null 또는 예외 발생 가능)
     */
    T findById(ID id);

    /**
     * 테이블의 모든 엔티티 목록을 조회합니다.
     *
     * @return 모든 엔티티 리스트
     */
    List<T> findAll();
}