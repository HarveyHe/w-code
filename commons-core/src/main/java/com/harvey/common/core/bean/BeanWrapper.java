package com.harvey.common.core.bean;

import java.util.List;

import org.springframework.beans.BeansException;

public interface BeanWrapper extends org.springframework.beans.BeanWrapper {
    void copyPropertiesTo(Object destinationBean, List<String> propertyNames);
    Object getPropertyValueRecursively(String propertyName) throws BeansException;
    Class<?> getPropertyTypeRecursively(String propertyName) throws BeansException;
}
