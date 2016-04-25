package com.harvey.common.core.spring.security.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;

public interface PreAuthHandler {
    void onPreAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException;
}
