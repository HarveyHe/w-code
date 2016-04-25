package com.harvey.common.core.spring.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

import com.harvey.common.core.context.Context;

public class ContextFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        try {
            Context.setRequest((HttpServletRequest) request,(HttpServletResponse)response);
            filterChain.doFilter(request, response);
        } finally {
            Context.releaseRequest();
        }
    }

}
