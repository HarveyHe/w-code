package com.harvey.common.core.spring.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;

public class EafAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, EafAuthenticationDetails> {

    
    @Override
    public EafAuthenticationDetails buildDetails(HttpServletRequest context) {
        return new EafAuthenticationDetails(context);
    }

}
