package com.harvey.common.core.spring.security.handler;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

public class RedirectSupoortAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler implements AuthFailureHandler {

    private static final Log log = LogFactory.getLog(RedirectSupoortAuthFailureHandler.class);
    
    private int order = 9999;
    
    private String exceptionCode = "code";
    private Map<Class<?>, String> exceptionCodeMap =  Collections.emptyMap();
    private String baseDefaultFailureUrl;
   
    private String parameterName;
    private String returnUrlParamName;
    
    public void setReturnUrlParamName(String returnUrlParamName) {
        this.returnUrlParamName = returnUrlParamName;
    }

    @Override
    public boolean isSupportHandler(HttpServletRequest request, AuthenticationException exception) {
        if(StringUtils.isNotBlank(parameterName)){
            return StringUtils.isNotBlank(request.getParameter(parameterName));
        }
        return true;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String code = getExceptionCode(exception);
        StringBuilder sb = new StringBuilder(baseDefaultFailureUrl);
        if (!(baseDefaultFailureUrl.indexOf('?') > 0)) {
            sb.append('?');
        } else {
            sb.append('&');
        }
        sb.append(exceptionCode).append('=').append(code);
        sb.append('&').append("type=").append(exception.getClass().getSimpleName());
        if(StringUtils.isNotEmpty(returnUrlParamName) && StringUtils.isNotEmpty(request.getParameter(returnUrlParamName))){
            sb.append('&').append(this.returnUrlParamName).append('=').append(URLEncoder.encode(request.getParameter(returnUrlParamName), "utf-8"));
        }
        super.setDefaultFailureUrl(sb.toString());
        super.onAuthenticationFailure(request, response, exception);
        
        if(exception instanceof AuthenticationServiceException && exception.getCause() != null){
            log.error("authentication failure", exception.getCause());
        }
    }

    private String getExceptionCode(AuthenticationException exception) {
        if (this.exceptionCodeMap != null && exception != null) {
            Class<?> clazz = exception.getClass();
            for (Entry<Class<?>, String> codeEntry : exceptionCodeMap.entrySet()) {
                if (codeEntry.getKey().isAssignableFrom(clazz)) {
                    return codeEntry.getValue();
                }
            }
        }
        return "00"; // 未知错误
    }

    public String getExceptionCode() {
        return exceptionCode;
    }

    public void setExceptionCode(String exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

    public Map<Class<?>, String> getExceptionCodeMap() {
        return exceptionCodeMap;
    }

    public void setExceptionCodeMap(Map<Class<?>, String> exceptionCodeMap) {
        this.exceptionCodeMap = exceptionCodeMap;
    }

    @Override
    public void setDefaultFailureUrl(String defaultFailureUrl) {
        baseDefaultFailureUrl = defaultFailureUrl;
        super.setDefaultFailureUrl(defaultFailureUrl);
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    @Override
    public int getOrder() {
        return order;
    }

}
