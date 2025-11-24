package com.my.project.domain;

import com.spring.sprout.global.annotation.db.Entity;
import lombok.Getter;

@Entity(table = "users")
@Getter
public class User {

    private int id;
    private String name;
    private int age;

    public User() {
    }

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
}