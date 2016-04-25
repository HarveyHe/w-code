package com.harvey.common.core.spring.security.handler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
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

import com.harvey.common.core.utils.HttpUtils;

public class RestAuthFailureHandler implements AuthFailureHandler {

   private static final Log log = LogFactory.getLog(RestAuthFailureHandler.class);
    
    private int order = 1;
    private String parameterName;
    private Map<Class<?>, String> exceptionCodeMap = Collections.emptyMap();
    private String exceptionCode = "code";
    
    @Override
    public boolean isSupportHandler(HttpServletRequest request, AuthenticationException exception) {
        if(StringUtils.isNotBlank(parameterName)){
            return StringUtils.isNotBlank(request.getParameter(parameterName));
        }
        return false;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        Map<String, Map<String, String>> result = new HashMap<String,Map<String, String>>();
        Map<String, String> error = new HashMap<String,String>();
        result.put("error",error);
        String code = getExceptionCode(exception);
        error.put(this.exceptionCode, code);
        error.put("type", exception.getClass().getSimpleName());
        HttpUtils.outJson(result, request, response);
        
        if(exception instanceof AuthenticationServiceException && exception.getCause() != null){
            log.error("authentication failure", exception.getCause());
        }
    }
    
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
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
    
    public void setExceptionCodeMap(Map<Class<?>, String> exceptionCodeMap) {
        this.exceptionCodeMap = exceptionCodeMap;
    }

    public void setExceptionCode(String exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

    @Override
    public int getOrder() {
        return order;
    }
    
    public void setOrder(int order){
        this.order = order;
    }
}
