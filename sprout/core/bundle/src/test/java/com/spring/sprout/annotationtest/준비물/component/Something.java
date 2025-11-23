package com.spring.sprout.annotationtest.준비물.component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Service
@Retention(RetentionPolicy.RUNTIME)
public @interface Something {

}
