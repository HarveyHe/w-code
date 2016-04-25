package com.harvey.common.core.spring.mvc;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import com.harvey.common.core.bean.BeanWrapper;
import com.harvey.common.core.bean.DefaultBeanWrapper;

public class HandlerMappingBeanPostProcessor implements BeanPostProcessor {

    private Map<String,Object> propertyValues;
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof RequestMappingInfoHandlerMapping){
            RequestMappingInfoHandlerMapping handlerMapping = (RequestMappingInfoHandlerMapping)bean;
            handlerMapping.setAlwaysUseFullPath(true);
            handlerMapping.setDetectHandlerMethodsInAncestorContexts(true);
            if(this.propertyValues != null){
                BeanWrapper bw = new DefaultBeanWrapper(bean);
                bw.setPropertyValues(propertyValues);
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public void setPropertyValues(Map<String, Object> propertyValues) {
        this.propertyValues = propertyValues;
    }

    
    

}
