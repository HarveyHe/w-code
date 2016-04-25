package com.harvey.common.core.spring;

import org.springframework.web.context.WebApplicationContext;

public class EafSpringRestServlet extends SpringDispatcherServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
        this.setConfigPattern("rest");
        return super.createWebApplicationContext(parent);
    }

}
