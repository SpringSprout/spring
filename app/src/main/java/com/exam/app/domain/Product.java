package com.exam.app.domain;

import com.spring.sprout.annotation.Entity;
import lombok.Getter;

@Entity
@Getter
public class Product {

    int id;
    String name;

    public Product() {
    }

    public Product(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "'}";
    }
}
