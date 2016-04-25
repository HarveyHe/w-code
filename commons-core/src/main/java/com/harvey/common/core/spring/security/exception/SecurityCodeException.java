package com.harvey.common.core.spring.security.exception;

import org.springframework.security.core.AuthenticationException;

public class SecurityCodeException extends AuthenticationException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SecurityCodeException(String msg) {
        super(msg);
    }

}
