package com.harvey.common.core.spring.security.handler;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

import com.harvey.common.core.utils.SortOrderUtils;

public class MultiAccessDeniedHandler extends AccessDeniedHandlerImpl implements ApplicationContextAware, InitializingBean {

    public Collection<AuthAccessDeniedHandler> getHandlers() {
        return handlers;
    }

    public void setHandlers(Collection<AuthAccessDeniedHandler> handlers) {
        this.handlers = handlers;
    }

    private Collection<AuthAccessDeniedHandler> handlers;
    private ApplicationContext applicationContext;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (handlers != null) {
            for (AuthAccessDeniedHandler handler : handlers) {
                if(handler.isSupportHandler(request, accessDeniedException)){
                    handler.handle(request, response, accessDeniedException);
                    return;
                }
            }
        }
        super.handle(request, response, accessDeniedException);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            handlers = SortOrderUtils.sort(this.applicationContext.getBeansOfType(AuthAccessDeniedHandler.class).values());
        } catch (Exception ex) {

        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
