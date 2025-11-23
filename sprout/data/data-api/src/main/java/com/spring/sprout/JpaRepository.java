package com.spring.sprout;

import java.util.List;

public interface JpaRepository<T, ID> {

    void save(T entity);

    T findById(ID id);

    List<T> findAll();
}
