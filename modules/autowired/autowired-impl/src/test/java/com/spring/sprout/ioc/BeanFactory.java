package com.spring.sprout.ioc;

import java.util.HashMap;
import lombok.Getter;

@Getter
public class BeanFactory {

    HashMap<Object, Object> beans = new HashMap<>();
}
