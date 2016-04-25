package com.harvey.common.core.spring;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.harvey.common.core.config.ConfigLoader;

public class SpringDispatcherServlet extends DispatcherServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String configPattern;

    public String getConfigPattern() {
        return configPattern;
    }

    public void setConfigPattern(String configPattern) {
        this.configPattern = configPattern;
    }

    @Override
    protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
        String pattern = "**/**/servletContext-" + (StringUtils.isEmpty(configPattern) ? getServletName() : configPattern) + ".xml";
        ConfigLoader loader = ConfigLoader.loadConfig(getServletContext());
        String configLocation = StringUtils.join(loader.getServletContextList(pattern), ",");
        super.setContextConfigLocation(configLocation);
        WebApplicationContext context =  super.createWebApplicationContext(parent);
        return context;
    }

}
