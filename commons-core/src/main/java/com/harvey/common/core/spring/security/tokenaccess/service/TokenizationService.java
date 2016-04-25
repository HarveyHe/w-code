package com.harvey.common.core.spring.security.tokenaccess.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;

import com.harvey.common.core.model.UserBaseModel;
import com.harvey.common.core.spring.security.tokenaccess.AccessAuthenticationToken;

public interface TokenizationService {

    boolean isTokenAccess(HttpServletRequest request,HttpServletResponse response);
    
    String obtainTokenFromRequest(HttpServletRequest request);
    
    UserBaseModel autoLogin(String accessToken,HttpServletRequest request,HttpServletResponse response) throws AuthenticationException;

    void authenticateToken(UserBaseModel user, AccessAuthenticationToken authentication) throws AuthenticationException;
}
