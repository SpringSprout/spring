package com.my.project.mvc;

import com.spring.sprout.global.annotation.controller.Controller;
import com.spring.sprout.global.annotation.controller.GetMapping;
import com.spring.sprout.global.annotation.controller.PostMapping;
import com.spring.sprout.global.annotation.controller.RequestMapping;
import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/")
@AllArgsConstructor
public class MyController {

    MyService myService;

    @GetMapping("/get")
    public void handleGet() {
        myService.get();
    }

    @PostMapping("/post")
    public void handlePost() {
        myService.post();
    }
}
