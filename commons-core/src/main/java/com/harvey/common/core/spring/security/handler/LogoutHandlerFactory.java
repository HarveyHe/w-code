package com.harvey.common.core.spring.security.handler;

import java.util.Collection;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.web.authentication.logout.LogoutHandler;

public class LogoutHandlerFactory implements FactoryBean<Collection<LogoutHandler>>, ApplicationContextAware {

    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Collection<LogoutHandler> getObject() throws Exception {
        return applicationContext.getBeansOfType(LogoutHandler.class).values();
    }

    @Override
    public Class<?> getObjectType() {
        return Collection.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
