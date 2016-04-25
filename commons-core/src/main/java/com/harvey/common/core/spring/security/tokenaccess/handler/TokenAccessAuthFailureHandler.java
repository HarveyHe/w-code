package com.harvey.common.core.spring.security.tokenaccess.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;

import com.harvey.common.core.utils.HttpUtils;

public class TokenAccessAuthFailureHandler implements AuthFailureHandler {

    private static final Log log = LogFactory.getLog(TokenAccessAuthFailureHandler.class);
    private int order = Integer.MAX_VALUE;
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        Map<String, String> error = new HashMap<String,String>();
        error.put("error","Bad Credentials");
        HttpUtils.outJson(error, request, response);
        
        if(exception instanceof AuthenticationServiceException && exception.getCause() != null){
            log.error("authentication failure", exception.getCause());
        }        
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean isSupportHandler(HttpServletRequest request, AuthenticationException exception) {
        return true;
    }
}
