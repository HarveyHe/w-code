package com.harvey.common.core.spring.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SaveContextOnUpdateOrErrorResponseWrapper;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.util.WebUtils;

import com.harvey.common.core.context.Context;
import com.harvey.common.core.spring.security.tokenaccess.AccessAuthenticationToken;

public class EafSecurityContextRepository extends HttpSessionSecurityContextRepository implements SecurityContextRepository {

    @Override
    public boolean containsContext(HttpServletRequest request) {
        if(isAccessByToken(request,null)){
            return true;
        }
        return super.containsContext(request);
    }
    
    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        if(isAccessByToken(requestResponseHolder.getRequest(),requestResponseHolder.getResponse())){
            return SecurityContextHolder.getContext();
        }
        return super.loadContext(requestResponseHolder);
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        if(isAccessByToken(request,response)){
            this.disableSaveContext(request, response);
            return;
        }
        super.saveContext(context, request, response);
    }
    
    public void disableSaveContext(HttpServletRequest request,HttpServletResponse response){
        SaveContextOnUpdateOrErrorResponseWrapper responseWrapper = WebUtils.getNativeResponse(response, SaveContextOnUpdateOrErrorResponseWrapper.class);
        if (responseWrapper != null) {
            responseWrapper.disableSaveOnResponseCommitted();
        }
    }
    
    private boolean isAccessByToken(HttpServletRequest request,HttpServletResponse response){
        return Context.getAuthentication() instanceof AccessAuthenticationToken;
    }
}
