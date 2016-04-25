package com.harvey.common.core.spring.mvc;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.RequestParamMethodArgumentResolver;

import com.harvey.common.core.utils.ReflectionUtils;

/**
 * 自适应 RequestParam注解
 * 
 * @author admin
 * 
 */
public class RequestParamArgumentResolver extends RequestParamMethodArgumentResolver {

    private boolean isTrimNullString;

    public RequestParamArgumentResolver(ConfigurableBeanFactory beanFactory, boolean useDefaultResolution, boolean isTrimNullString) {
        super(beanFactory, useDefaultResolution);
        this.isTrimNullString = isTrimNullString;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (BeanUtils.isSimpleProperty(parameter.getParameterType())) {
            return true;
        }
        return super.supportsParameter(parameter);
    }

    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        NamedValueInfo namedValueInfo = null;
        String[] names = ReflectionUtils.getMethodParameterNames(parameter.getMethod());
        if (names.length > 0 && parameter.getParameterIndex() > -1 && parameter.getParameterIndex() < names.length) {
            String name = names[parameter.getParameterIndex()];
            if (parameter.getParameterType().isArray()) {
                name += "[]";
            }
            namedValueInfo = new NamedValueInfo(name, false, ValueConstants.DEFAULT_NONE);
        } else {
            namedValueInfo = super.createNamedValueInfo(parameter);
        }
        return namedValueInfo;
    }

    @Override
    protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest webRequest) throws Exception {
        if (isTrimNullString) {
            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            if ("null".equals(request.getParameter(name))) {
                return null;
            }
        }
        return super.resolveName(name, parameter, webRequest);
    }

}
