package com.harvey.common.core.utils;

import java.util.Map;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import com.harvey.common.core.context.Context;

/**
 * Model and view utils
 * @author dream
 *
 */
public class MVUtils {
	
	private static String detectUrl(String url){
		if(url.startsWith("http://") || url.startsWith("https://")){
			return url;
		}
		String contextPath = Context.getContextPath();
		if(url.startsWith(contextPath)){
			return url;
		}else if(url.charAt(0)=='/'){
			return contextPath + url;
		}else{
			return contextPath + '/' + url;
		}
	}
	
	public static String forward(String url){
		return UrlBasedViewResolver.FORWARD_URL_PREFIX + detectUrl(url);
	}
	
	public static String redirect(String url){
		return UrlBasedViewResolver.REDIRECT_URL_PREFIX + detectUrl(url);
	}
	
	public static ModelAndView forward(String url,Object model){
		ModelAndView mv = new ModelAndView(forward(url));
		mv.addObject(model);
		return mv;
	}
	public static ModelAndView forward(String url,Map<String,?> model){
		ModelAndView mv = new ModelAndView(forward(url),model);
		return mv;
	}
	public static ModelAndView forward(String url,String mvName,Object model){
		ModelAndView mv = new ModelAndView(forward(url),mvName,model);
		return mv;
	}
	
	public static ModelAndView redirect(String url,Object model){
		ModelAndView mv = new ModelAndView(redirect(url));
		mv.addObject(model);
		return mv;
	}
	public static ModelAndView redirect(String url,Map<String,?> model){
		ModelAndView mv = new ModelAndView(redirect(url),model);
		return mv;
	}
	public static ModelAndView redirect(String url,String mvName,Object model){
		ModelAndView mv = new ModelAndView(redirect(url),mvName,model);
		return mv;
	}
}
