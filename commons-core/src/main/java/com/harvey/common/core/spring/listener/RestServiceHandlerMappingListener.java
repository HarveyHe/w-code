package com.harvey.common.core.spring.listener;

import java.lang.reflect.Method;

public interface RestServiceHandlerMappingListener {
    void onServiceHandlerMapping(String url,Class<?> beanType,Method method);
}
