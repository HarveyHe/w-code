package com.harvey.common.core.exception;

import org.springframework.security.access.AccessDeniedException;

public class NoneLoginException extends AccessDeniedException {

    public NoneLoginException(){
        this("Login please!");
    }
    
    public NoneLoginException(String msg) {
        super(msg);
    }

}
