package com.harvey.common.core.spring.security.tokenaccess.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import com.harvey.common.core.spring.security.tokenaccess.handler.AuthFailureHandler;
import com.harvey.common.core.spring.security.tokenaccess.provider.TokenAuthenticationProvider;
import com.harvey.common.core.utils.SortOrderUtils;

public class TokenAuthenticationFilter extends GenericFilterBean implements ApplicationContextAware {

    private final Log log = LogFactory.getLog(this.getClass());

    private ApplicationContext applicationContext;
    private List<TokenAuthenticationProvider> providers;
    private List<AuthFailureHandler> failureHandlers;

    public TokenAuthenticationProvider getProvider(ServletRequest request, ServletResponse response) {
        /*Object obj = request.getAttribute(TokenAuthenticationProvider.class.getSimpleName());
        TokenAuthenticationProvider provider = (obj instanceof TokenAuthenticationProvider) ? (TokenAuthenticationProvider) obj : null;
        if (provider != null) {
            return provider;
        }
        if (obj != null) {
            return null;
        }*/
        TokenAuthenticationProvider provider = null;
        for (TokenAuthenticationProvider temp : providers) {
            if (temp.isSupported((HttpServletRequest) request, (HttpServletResponse) response)) {
                provider = temp;
                break;
            }
        }
        //request.setAttribute(TokenAuthenticationProvider.class.getSimpleName(), provider == null ? Boolean.FALSE : provider);
        return provider;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        TokenAuthenticationProvider provider = getProvider(request, response);
        if (provider != null) {
            try {
                Authentication authentication = provider.attemptAuthentication((HttpServletRequest) request, (HttpServletResponse) response);
                if (authentication.isAuthenticated()) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (AuthenticationException ex) {
                if (log.isDebugEnabled()) {
                    log.error(ex.getMessage(), ex);
                }
                if (failureHandlers != null) {
                    for (AuthFailureHandler handler : failureHandlers) {
                        if (handler.isSupportHandler((HttpServletRequest) request, ex)) {
                            handler.onAuthenticationFailure((HttpServletRequest) request, (HttpServletResponse) response, ex);
                            return;
                        }
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws ServletException {
        super.afterPropertiesSet();
        if (providers == null) {
            providers = new ArrayList<TokenAuthenticationProvider>(SortOrderUtils.sort(this.applicationContext.getBeansOfType(
                    TokenAuthenticationProvider.class).values()));
        }

        if (failureHandlers == null) {
            failureHandlers = new ArrayList<AuthFailureHandler>(SortOrderUtils.sort(this.applicationContext.getBeansOfType(AuthFailureHandler.class)
                    .values()));
        }
        Assert.notEmpty(providers);
        // /Assert.notEmpty(failureHandlers);
    }

    public List<TokenAuthenticationProvider> getProviders() {
        return providers;
    }

    public void setProviders(List<TokenAuthenticationProvider> providers) {
        this.providers = providers;
    }

    public List<AuthFailureHandler> getFailureHandlers() {
        return failureHandlers;
    }

    public void setFailureHandlers(List<AuthFailureHandler> failureHandlers) {
        this.failureHandlers = failureHandlers;
    }

}
