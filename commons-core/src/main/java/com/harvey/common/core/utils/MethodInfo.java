package com.harvey.common.core.utils;

import java.lang.reflect.Method;
import java.util.Map;

public class MethodInfo {
    private Method method;
    private Map<String, Class<?>> parameters;
    private Integer index;

    public MethodInfo(Method method, Map<String, Class<?>> parameters) {
        this.method = method;
        this.parameters = parameters;
    }

    public Method getMethod() {
        return method;
    }

    public Map<String, Class<?>> getParameters() {
        return parameters;
    }
    
    public Class<?>[] getParameterTypes(){
        return this.method.getParameterTypes();
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}
