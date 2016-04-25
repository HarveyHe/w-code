package com.harvey.common.core.spring;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;

import com.harvey.common.core.config.Config;
import com.harvey.common.core.config.ConfigLoader;
import com.harvey.common.core.context.Context;

public class SpringContextLoaderListener extends ContextLoaderListener {

    @Override
    protected void customizeContext(ServletContext servletContext, ConfigurableWebApplicationContext context) {
        try {
            ConfigLoader loader = ConfigLoader.loadConfig(servletContext);
            Context.setContext(context);
            Context.setServletContext(servletContext);
            String[] configLocations = new String[loader.getAppContextList().size()];
            context.setConfigLocations(loader.getAppContextList().toArray(configLocations));
            servletContext.setAttribute("config", Config.getProperties());
            super.customizeContext(servletContext, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        Context.setServletContext(event.getServletContext());
        super.contextInitialized(event);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Context.setServletContext(null);
        Context.setContext(null);
        super.contextDestroyed(event);
    }

}
