package com.my.project.mvc;

import com.my.project.mvc.dto.MemberInfo;
import com.spring.sprout.global.annotation.controller.Controller;
import com.spring.sprout.global.annotation.controller.GetMapping;
import com.spring.sprout.global.annotation.controller.PostMapping;
import com.spring.sprout.global.annotation.controller.RequestMapping;
import java.util.List;
import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/")
@AllArgsConstructor
public class MyController {

    MyService myService;

    @GetMapping("/members")
    public List<MemberInfo> getMembers() {
        return myService.getMembers();
    }

    @PostMapping("/register")
    public void register() {
        myService.register();
    }
}
