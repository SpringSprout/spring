package com.spring.sprout;

public interface JpaRepository<T, ID> {

    void save(T entity);

    T findById(ID id);
}
