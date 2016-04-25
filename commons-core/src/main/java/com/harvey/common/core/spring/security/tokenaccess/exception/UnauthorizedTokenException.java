package com.harvey.common.core.spring.security.tokenaccess.exception;

import org.springframework.security.core.AuthenticationException;

public class UnauthorizedTokenException extends AuthenticationException {

    private int httpErrorCode;
    
    /**
     * 
     */
    private static final long serialVersionUID = -8806162868030764455L;

    public UnauthorizedTokenException(String msg) {
        this(msg,401);
    }
    
    public UnauthorizedTokenException(String msg,int httpErrorCode) {
        super(msg);
        this.httpErrorCode = httpErrorCode;
    }
    
    public int getHttpErrorCode() {
        return this.httpErrorCode;
    }

}
