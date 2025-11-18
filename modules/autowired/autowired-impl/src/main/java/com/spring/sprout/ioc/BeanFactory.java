package com.spring.sprout.ioc;

import java.util.HashMap;
import lombok.Getter;

@Getter
public class BeanFactory {

    HashMap<String, Object> beans = new HashMap<>();
}
