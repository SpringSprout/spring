package com.my.project.repository;

import com.my.project.domain.User;
import com.spring.sprout.JpaRepository;
import com.spring.sprout.global.annotation.db.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

}
