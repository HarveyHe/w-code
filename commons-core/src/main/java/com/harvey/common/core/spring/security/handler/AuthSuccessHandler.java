package com.harvey.common.core.spring.security.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;

import com.harvey.common.core.utils.SortOrder;

/**
 * 提供登录成功跳转或ajax登录成返回的处理能力
 * @author admin
 *
 */
public interface AuthSuccessHandler extends SortOrder {
    
    boolean isSupportHandler(HttpServletRequest request,Authentication authentication);
    
    void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException;
}
