package com.my.project.mvc;

import com.spring.sprout.global.annotation.Service;

@Service
public class MyService {

    public void get() {
        System.out.println("Get method in MyService!");
    }

    public void post() {
        System.out.println("Post method in MyService!");
    }
}
