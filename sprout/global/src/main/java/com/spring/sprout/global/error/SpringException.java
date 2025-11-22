package com.spring.sprout.global.error;

public class SpringException extends RuntimeException {

    public SpringException(ErrorMessage message) {
        super(message.getMessage());
    }
}
