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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import com.harvey.common.core.utils.SortOrderUtils;

public class MultiAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private Collection<AuthFailureHandler> failureHandlers;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        try{
            failureHandlers = SortOrderUtils.sort(this.applicationContext.getBeansOfType(AuthFailureHandler.class).values());
        }catch(Exception ex){
            this.failureHandlers = Collections.emptyList();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        for(AuthFailureHandler failureHandler:this.failureHandlers){
            if(failureHandler.isSupportHandler(request, exception)){
                failureHandler.onAuthenticationFailure(request, response, exception);
                return;
            }
        }
        super.onAuthenticationFailure(request, response, exception);
    }

}
