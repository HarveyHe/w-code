package com.harvey.common.core.utils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.harvey.common.core.context.Context;

public class DispactherServletUtils {
    
    private static Method getMethod(Class<?> clazz,String name){
        for(Method method:clazz.getDeclaredMethods()){
            if(method.getName().equals(name)){
                return method;
            }
        }
        return null;
    }
    
    public static void registerMethodHandler(String servletName, Object methodHandler, Method method, String... urlPatterns) {
        ApplicationContext context = Context.getServletApplicationContext(servletName);
        Collection<RequestMappingHandlerMapping> handlerMappings = context.getBeansOfType(RequestMappingHandlerMapping.class).values();
        if (!handlerMappings.isEmpty()) {
            registerMethodHandler(handlerMappings.iterator().next(), methodHandler, method, urlPatterns);
        }
    }

    public static void registerMethodHandler(RequestMappingHandlerMapping methodMapping, Object methodHandler, Method method, String... urlPatterns) {
        try {
            Class<?> handlerType = method.getDeclaringClass();
            Method getMappingForMethod = getMethod(RequestMappingHandlerMapping.class,"getMappingForMethod");
            getMappingForMethod.setAccessible(true);
            RequestMappingInfo mi = (RequestMappingInfo) getMappingForMethod.invoke(methodMapping, method, handlerType);
            PatternsRequestCondition condition = new PatternsRequestCondition(urlPatterns,methodMapping.getUrlPathHelper(),methodMapping.getPathMatcher(),methodMapping.useSuffixPatternMatch(),methodMapping.useTrailingSlashMatch(),methodMapping.getFileExtensions())
                                                 .combine(mi.getPatternsCondition());
            RequestMappingInfo mappingInfo = new RequestMappingInfo(condition, mi.getMethodsCondition(), mi.getParamsCondition(), mi.getHeadersCondition(), mi.getConsumesCondition(), mi.getProducesCondition(), mi.getCustomCondition());
            Method registerHandlerMethod = getMethod(AbstractHandlerMethodMapping.class,"registerHandlerMethod");
            registerHandlerMethod.setAccessible(true);
            registerHandlerMethod.invoke(methodMapping, methodHandler, method, mappingInfo);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void registerHandler(String servletName, String urlPattern, Controller controller) {
        ApplicationContext context = Context.getServletApplicationContext(servletName);
        Collection<AbstractUrlHandlerMapping> handlerMappings = context.getBeansOfType(AbstractUrlHandlerMapping.class).values();
        if (!handlerMappings.isEmpty()) {
            registerHandler(handlerMappings.iterator().next(), urlPattern, controller);
        }
    }

    public static void registerHandlers(String servletName, Map<String, Controller> handlers) {
        ApplicationContext context = Context.getServletApplicationContext(servletName);
        Collection<AbstractUrlHandlerMapping> handlerMappings = context.getBeansOfType(AbstractUrlHandlerMapping.class).values();
        if (!handlerMappings.isEmpty()) {
            AbstractUrlHandlerMapping handlerMapping = handlerMappings.iterator().next();
            for (Entry<String, Controller> entry : handlers.entrySet()) {
                registerHandler(handlerMapping, entry.getKey(), entry.getValue());
            }
        }
    }

    public static void registerHandler(AbstractUrlHandlerMapping handlerMapping, String urlPattern, Controller controller) {
        try {
            Method method = AbstractUrlHandlerMapping.class.getDeclaredMethod("registerHandler", String.class, Object.class);
            method.setAccessible(true);
            method.invoke(handlerMapping, urlPattern, controller);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
