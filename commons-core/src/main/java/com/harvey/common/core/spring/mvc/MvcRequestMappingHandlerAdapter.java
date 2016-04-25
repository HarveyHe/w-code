package com.harvey.common.core.spring.mvc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletRequest;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.InitBinderDataBinderFactory;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletRequestDataBinderFactory;
import org.springframework.web.util.WebUtils;

import com.harvey.common.core.context.Context;
import com.harvey.common.core.spring.editor.CustomDateEditor;
import com.harvey.common.core.spring.editor.CustomStringArrayEditor;
import com.harvey.common.core.spring.rest.RestServiceHandlerAdapter;
import com.harvey.common.core.utils.HttpUtils;

public class MvcRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {
    
    private boolean isRegisterCustomEditor = true;
    private boolean isTrimNullString = true;
    
    @Override
    public int getOrder() {
        int order = super.getOrder();
        return order == Integer.MAX_VALUE ? 0 : order;
    }
    
    private RequestMappingHandlerAdapter getDefaultRequestMappingHandlerAdapter() {
        for (RequestMappingHandlerAdapter handler : this.getApplicationContext().getBeansOfType(RequestMappingHandlerAdapter.class).values()) {
            if (!(handler instanceof RestServiceHandlerAdapter)) {
                return handler;
            }
        }
        return null;
    }
    
    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<HandlerMethodArgumentResolver>(this.getArgumentResolvers());
        resolvers.add(0, new RequestParamArgumentResolver(getBeanFactory(), false,isTrimNullString));
        this.setArgumentResolvers(resolvers);
        WebBindingInitializerComposite bindingInitializer = new WebBindingInitializerComposite();
        bindingInitializer.addWebBindingInitializer(this.getWebBindingInitializer());
        RequestMappingHandlerAdapter defaultAdapter = getDefaultRequestMappingHandlerAdapter();
        if (defaultAdapter != null) {
            bindingInitializer.addWebBindingInitializer(defaultAdapter.getWebBindingInitializer());
        }        
        bindingInitializer.addWebBindingInitializers(this.getApplicationContext().getBeansOfType(WebBindingInitializer.class).values());
        bindingInitializer.addWebBindingInitializers(Context.getContext().getBeansOfType(WebBindingInitializer.class).values());
        if(this.isRegisterCustomEditor){
            bindingInitializer.addWebBindingInitializer(new WebBindingInitializer() {
                
                @Override
                public void initBinder(WebDataBinder binder, WebRequest request) {
                    binder.registerCustomEditor(Date.class, new CustomDateEditor());
                    binder.registerCustomEditor(String[].class, new CustomStringArrayEditor());
                }
            });
        }
        this.setWebBindingInitializer(bindingInitializer);
    }

    @Override
    protected InitBinderDataBinderFactory createDataBinderFactory(List<InvocableHandlerMethod> binderMethods) throws Exception {
        if(isTrimNullString){
            return this.createTrimNullStringDataBinderFactory(binderMethods);
        }
        return super.createDataBinderFactory(binderMethods);
    }
    
    protected InitBinderDataBinderFactory createTrimNullStringDataBinderFactory(List<InvocableHandlerMethod> binderMethods) throws Exception {
        return new ServletRequestDataBinderFactory(binderMethods, getWebBindingInitializer()){

            @Override
            protected ServletRequestDataBinder createBinderInstance(Object target, String objectName, NativeWebRequest request) {
                return new ExtendedServletRequestDataBinder(target,objectName){

                    @Override
                    public void bind(ServletRequest request) {
                        MutablePropertyValues mpvs = new MutablePropertyValues(HttpUtils.getParametersStartingWith(request, null, isTrimNullString));
                        MultipartRequest multipartRequest = WebUtils.getNativeRequest(request, MultipartRequest.class);
                        if (multipartRequest != null) {
                            bindMultipart(multipartRequest.getMultiFileMap(), mpvs);
                        }
                        addBindValues(mpvs, request);
                        doBind(mpvs);
                    }
                    
                };
            }
            
        };
    }

    public void setRegisterCustomEditor(boolean isRegisterCustomEditor) {
        this.isRegisterCustomEditor = isRegisterCustomEditor;
    }

    public void setTrimNullString(boolean isTrimNullString) {
        this.isTrimNullString = isTrimNullString;
    }
    
    
}
