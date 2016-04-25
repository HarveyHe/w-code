package com.harvey.common.core.spring.rest;


import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.harvey.common.core.model.Errors;

public class RestHandlerMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return true;
	}

	@Override
	public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest)
	        throws Exception {
		if (returnValue != null) {
			if (Errors.class.isAssignableFrom(returnType.getParameterType())) {
				mavContainer.addAttribute("errors", returnValue);
			} else {
				mavContainer.addAttribute("data", returnValue);
			}
		}
	}
}
