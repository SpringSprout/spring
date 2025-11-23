package com.my.project.mvc;

import com.my.project.mvc.dto.MemberInfo;
import com.spring.sprout.global.annotation.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class MyService {

    public List<MemberInfo> getMembers() {
        List<MemberInfo> memberInfos = new ArrayList<>();
        memberInfos.add(new MemberInfo(1L, "Alice"));
        memberInfos.add(new MemberInfo(2L, "Bob"));

        return memberInfos;
    }

    public void register() {
    }
}
