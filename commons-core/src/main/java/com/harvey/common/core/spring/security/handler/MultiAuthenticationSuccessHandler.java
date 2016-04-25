package com.harvey.common.core.spring.security.handler;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import com.harvey.common.core.utils.SortOrderUtils;

public class MultiAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private Collection<AuthSuccessHandler> successHandlers;

    @Override
    public void afterPropertiesSet() throws Exception {
        try{
            this.successHandlers = SortOrderUtils.sort(this.applicationContext.getBeansOfType(AuthSuccessHandler.class).values());
        }catch(Exception ex){
            this.successHandlers = Collections.emptyList();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        for (AuthSuccessHandler successHandler : this.successHandlers) {
            if(successHandler.isSupportHandler(request, authentication)){
                successHandler.onAuthenticationSuccess(request, response, authentication);
                return;
            }
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }

}
