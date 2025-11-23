package com.spring.sprout.annotationtest.준비물.notcomponent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@NotComponent
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {

}
