package com.harvey.common.core.spring.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import com.harvey.common.core.spring.security.handler.AuthAccessDeniedHandler;
import com.harvey.common.core.spring.security.handler.MultiAccessDeniedHandler;

public class AuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private MultiAccessDeniedHandler accessDeniedHandler;

    private String returnUrlParamName;

    public void setReturnUrlParamName(String returnUrlParamName) {
        this.returnUrlParamName = returnUrlParamName;
    }

    public AuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if (accessDeniedHandler != null) {
            AccessDeniedException exception = new AccessDeniedException(authException.getMessage());
            for (AuthAccessDeniedHandler handler : accessDeniedHandler.getHandlers()) {
                if (handler.isSupportHandler(request, exception)) {
                    handler.handle(request, response, exception);
                    return;
                }
            }
        }
        super.commence(request, response, authException);
    }

    public MultiAccessDeniedHandler getAccessDeniedHandler() {
        return accessDeniedHandler;
    }

    public void setAccessDeniedHandler(MultiAccessDeniedHandler accessDeniedHandler) {
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        String url = super.determineUrlToUseForThisRequest(request, response, exception);
        if (StringUtils.isNotBlank(returnUrlParamName)) {
            StringBuilder sb = new StringBuilder(url);
            try {
                StringBuilder uri = new StringBuilder(request.getRequestURI());
                Enumeration<String> paramNames = request.getParameterNames();
                if(paramNames.hasMoreElements()){
                    uri.append('?');
                }
                for (; paramNames.hasMoreElements();) {
                    String name = paramNames.nextElement();
                    uri.append(name).append('=').append(request.getParameter(name));
                    if(paramNames.hasMoreElements()){
                        uri.append('&');
                    }
                }
                if (url.indexOf('?') < 0) {
                    sb.append('?').append(returnUrlParamName).append('=').append(URLEncoder.encode(URLEncoder.encode(uri.toString(), "UTF-8"), "UTF-8"));
                } else {
                    sb.append('&').append(returnUrlParamName).append('=').append(URLEncoder.encode(URLEncoder.encode(uri.toString(), "UTF-8"), "UTF-8"));
                }
                url = sb.toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return url;
    }

}
