package com.harvey.common.core.context;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.DispatcherServlet;

import com.harvey.common.core.exception.NoneLoginException;
import com.harvey.common.core.model.UserBaseModel;

public class Context {

    private static ApplicationContext context;
    private static ServletContext servletContext;
    private static final ThreadLocal<RequestInfo> requestThreadLocal = new ThreadLocal<RequestInfo>();
    private static CurrentUserDelegate userDelegate;

    public static WebApplicationContext getServletApplicationContext(String servletName) {
        return WebApplicationContextUtils.getWebApplicationContext(servletContext, DispatcherServlet.SERVLET_CONTEXT_PREFIX + servletName);
    }

    public static ApplicationContext getContext() {
        return context;
    }

    public static void setContext(ApplicationContext context) {
        Context.context = context;
    }

    public static HttpServletRequest getRequest() {
        return requestThreadLocal.get().getRequest();
    }
    
    public static HttpServletResponse getResponse() {
        return requestThreadLocal.get().getResponse();
    }

    public static void setRequest(HttpServletRequest request,HttpServletResponse response) {
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            request.setAttribute("userInfo", authentication.getPrincipal());
        }
        requestThreadLocal.set(new RequestInfo(request, response));
    }

    public static void releaseRequest() {
        requestThreadLocal.remove();
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanId) {
        return (T) getContext().getBean(beanId);
    }

    public static <T> T getBean(Class<T> type) {
        return getContext().getBean(type);
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static <T extends UserBaseModel> T getCurrentUser() {
        if (userDelegate == null) {
            synchronized (CurrentUserDelegate.class) {
                if (userDelegate == null) {
                    try {
                        userDelegate = getBean(CurrentUserDelegate.class);
                    } catch (Exception ex) {

                    }
                    if(userDelegate == null){
                        userDelegate = new DefaultCurrentUserDelegate();
                    }
                }
            }
        }
        return userDelegate.getCurrentUser();
    }

    public static <T extends UserBaseModel> T checkCurrentUser() {
        T user = getCurrentUser();
        if (user == null) {
            throw new NoneLoginException();
        }
        return user;
    }

    public static ServletContext getServletContext() {

        return servletContext;
    }

    public static void setServletContext(ServletContext servletContext) {
        Context.servletContext = servletContext;
    }

    public static String getContextPath() {
        return Context.getServletContext().getContextPath();
    }

    static class RequestInfo {
        
        private HttpServletRequest request;
        private HttpServletResponse response;
        
        public RequestInfo(HttpServletRequest request,HttpServletResponse response){
            this.request = request;
            this.response = response;
        }
        
        public HttpServletRequest getRequest() {
            return request;
        }
        public void setRequest(HttpServletRequest request) {
            this.request = request;
        }
        public HttpServletResponse getResponse() {
            return response;
        }
        public void setResponse(HttpServletResponse response) {
            this.response = response;
        }
        
    }
}
