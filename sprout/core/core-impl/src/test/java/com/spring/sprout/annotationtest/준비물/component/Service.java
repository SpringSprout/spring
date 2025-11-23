package com.spring.sprout.annotationtest.준비물.component;

import com.spring.sprout.global.annotation.Component;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Component
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {

}
