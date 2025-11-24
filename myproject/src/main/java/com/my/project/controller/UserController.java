package com.my.project.controller;

import com.my.project.dto.UserInfo;
import com.my.project.service.UserService;
import com.spring.sprout.global.annotation.Autowired;
import com.spring.sprout.global.annotation.controller.Controller;
import com.spring.sprout.global.annotation.controller.GetMapping;
import com.spring.sprout.global.annotation.controller.PostMapping;
import com.spring.sprout.global.annotation.controller.RequestBody;
import com.spring.sprout.global.annotation.controller.RequestMapping;
import java.util.List;
import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/")
@AllArgsConstructor
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/join")
    public void join(@RequestBody UserInfo userInfo) {
        userService.join(userInfo);
    }

    @PostMapping("/find")
    public UserInfo findUser(int id) {
        return userService.findInfo(id);
    }

    @GetMapping("/users")
    public List<UserInfo> findAll() {
        return userService.findAll();
    }
}
