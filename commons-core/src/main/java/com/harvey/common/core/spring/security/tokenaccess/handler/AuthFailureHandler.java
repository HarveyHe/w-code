package com.harvey.common.core.spring.security.tokenaccess.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;

import com.harvey.common.core.utils.SortOrder;

/**
 * Token登录失败处理
 * @author admin
 *
 */
public interface AuthFailureHandler extends SortOrder {
    boolean isSupportHandler(HttpServletRequest request,AuthenticationException exception);
    
    void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException;
}
