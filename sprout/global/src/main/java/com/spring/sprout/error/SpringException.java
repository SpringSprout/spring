package com.spring.sprout.error;

public class SpringException extends RuntimeException {

    public SpringException(ErrorMessage message) {
        super(message.getMessage());
    }
    
}
