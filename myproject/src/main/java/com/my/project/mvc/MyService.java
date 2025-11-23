package com.my.project.mvc;

import com.my.project.mvc.dto.MemberInfo;
import com.spring.sprout.global.annotation.Service;

@Service
public class MyService {

    public MemberInfo get() {
        return new MemberInfo(1L, "우아한테크코스");
    }

    public void post() {
    }
}
