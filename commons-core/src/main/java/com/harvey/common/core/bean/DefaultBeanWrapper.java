package com.harvey.common.core.bean;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;

import com.harvey.common.core.spring.editor.CustomDateEditor;
import com.harvey.common.core.spring.editor.CustomStringArrayEditor;

public class DefaultBeanWrapper extends BeanWrapperImpl implements BeanWrapper {

    public DefaultBeanWrapper(){
        super();
        registerCustomEditor(Date.class, new CustomDateEditor());
        registerCustomEditor(String[].class, new CustomStringArrayEditor());
    }
    
    public DefaultBeanWrapper(Object object) {
        super(object);
        registerCustomEditor(Date.class, new CustomDateEditor());
        registerCustomEditor(String[].class, new CustomStringArrayEditor());
    }

    public DefaultBeanWrapper(Class<?> clazz) {
        super(clazz);
        registerCustomEditor(Date.class, new CustomDateEditor());
        registerCustomEditor(String[].class, new CustomStringArrayEditor());
    }

    public void copyPropertiesTo(Object destinationBean, List<String> propertyNames) {
        DefaultBeanWrapper destinationBeanWrapper = new DefaultBeanWrapper(destinationBean);
        for (String propertyName : propertyNames) {
            destinationBeanWrapper.setPropertyValue(propertyName, getPropertyValue(propertyName));
        }
    }
    
    public Object getPropertyValueRecursively(String propertyName) throws BeansException {
        int dotIndex = propertyName.indexOf(".");
        if (dotIndex == -1) {
            return getPropertyValue(propertyName);
        } else {
            Object propertyBean = getPropertyValue(propertyName.substring(0, dotIndex));
            if (propertyBean == null) {
                return null;
            } else {
                return new DefaultBeanWrapper(propertyBean).getPropertyValueRecursively(
                        propertyName.substring(dotIndex + 1));
            }
        }
    }
    
    public Class<?> getPropertyTypeRecursively(String propertyName) throws BeansException {
        int dotIndex = propertyName.indexOf(".");
        if (dotIndex == -1) {
            return getPropertyType(propertyName);
        } else {
            Object propertyBean = getPropertyValue(propertyName.substring(0, dotIndex));
            if (propertyBean == null) {
                return null;
            } else {
                return new DefaultBeanWrapper(propertyBean).getPropertyTypeRecursively(
                        propertyName.substring(dotIndex + 1));
            }
        }
    }
    
    @Override
    public Object getPropertyValue(String propertyName) throws BeansException {
        if (this.getWrappedInstance() instanceof Map) {
            return ((Map<?,?>) this.getWrappedInstance()).get(propertyName);
        } else {
            return super.getPropertyValue(propertyName);
        }
    }
}
