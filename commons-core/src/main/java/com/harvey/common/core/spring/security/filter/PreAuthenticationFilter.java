package com.harvey.common.core.spring.security.filter;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.filter.GenericFilterBean;

import com.harvey.common.core.spring.security.handler.MultiAuthenticationFailureHandler;
import com.harvey.common.core.spring.security.handler.PreAuthHandler;

/**
 * 在验证用户登录前提供验证码之类的行为处理过程
 * 
 * @author admin
 * 
 */
public class PreAuthenticationFilter extends GenericFilterBean implements ApplicationContextAware {

    private Collection<PreAuthHandler> preAuthentications;
    private ApplicationContext applicationContext;
    private AuthenticationFailureHandler failureHandler;

    private String filterProcessesUrl;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse rep = (HttpServletResponse)response;
        if(!this.matches(req)){
            filterChain.doFilter(request, response);
            return;
        }
        try {
            for (PreAuthHandler preAuthentication : this.preAuthentications) {
                preAuthentication.onPreAuthentication(req, rep);
            }
            filterChain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            this.failureHandler.onAuthenticationFailure(req, rep, ex);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void initFilterBean() throws ServletException {
        try {
            this.preAuthentications = this.applicationContext.getBeansOfType(PreAuthHandler.class).values();
        } catch (Exception ex) {
        }
        try {
            this.failureHandler = this.applicationContext.getBean(AuthenticationFailureHandler.class);
        } catch (Exception ex) {
        }
        if (this.failureHandler == null) {
            MultiAuthenticationFailureHandler handler = new MultiAuthenticationFailureHandler();
            handler.setApplicationContext(this.applicationContext);
            try {
                handler.afterPropertiesSet();
            } catch (Exception e) {
                e.printStackTrace();
            }
            failureHandler = handler;
        }
        super.initFilterBean();
    }

    public void setFilterProcessesUrl(String filterProcessesUrl) {
        this.filterProcessesUrl = filterProcessesUrl;
    }
    
    private boolean matches(HttpServletRequest request) {
        if(StringUtils.isEmpty(filterProcessesUrl)){
            return false;
        }
        String uri = request.getRequestURI();
        int pathParamIndex = uri.indexOf(';');

        if (pathParamIndex > 0) {
            // strip everything after the first semi-colon
            uri = uri.substring(0, pathParamIndex);
        }

        if ("".equals(request.getContextPath())) {
            return uri.endsWith(filterProcessesUrl);
        }

        return uri.endsWith(request.getContextPath() + filterProcessesUrl);
    }
}
