package com.harvey.common.core.spring.rest;

import java.util.ArrayList;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;

import com.harvey.common.core.spring.mvc.MvcRequestMappingHandlerAdapter;
import com.harvey.common.core.utils.JSON;

public class RestServiceHandlerAdapter extends MvcRequestMappingHandlerAdapter {

	@Override
	public void afterPropertiesSet() {
		
		if (this.getCustomReturnValueHandlers() == null) {
			this.setCustomReturnValueHandlers(new ArrayList<HandlerMethodReturnValueHandler>());
		}
		this.getCustomReturnValueHandlers().add(new RestHandlerMethodReturnValueHandler());

        // json转换,传入的Content type为application/json,自动使用json转换
		boolean hasJsonConvter = false;
		for(HttpMessageConverter<?> converter : this.getMessageConverters()){
		    if(converter instanceof MappingJackson2HttpMessageConverter){
		        hasJsonConvter = true;
		        break;
		    }
		}
		if(!hasJsonConvter){
		    this.getMessageConverters().add(initJsonMessageConvter());
		}
		super.afterPropertiesSet();
	}
	
	private MappingJackson2HttpMessageConverter initJsonMessageConvter(){
	    MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter(JSON.buildJsonObjectMapper());
	    return jsonConverter;
	}
	

}
