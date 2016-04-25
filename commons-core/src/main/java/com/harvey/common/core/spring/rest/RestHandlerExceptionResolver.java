package com.harvey.common.core.spring.rest;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.PriorityOrdered;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.harvey.common.core.exception.NoneLoginException;
import com.harvey.common.core.exception.ValidationException;
import com.harvey.common.core.utils.HttpUtils;

public class RestHandlerExceptionResolver implements HandlerExceptionResolver,PriorityOrdered {
	
	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		return new ModelAndView(new RestExceptionView(ex));
	}

	private static class RestExceptionView implements View {

		/**
	     * Log variable for all child classes.
	     */
	    protected final Log log = LogFactory.getLog(getClass());
	    
		private Exception ex;

		public RestExceptionView(Exception ex) {
			this.ex = ex;
		}

		@Override
		public String getContentType() {
			return "application/json;charset=utf-8";
		}

		@Override
		public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
			Map<String, Object> map = new HashMap<String, Object>();
			if (ex instanceof ValidationException) {
				map.put("errors", ((ValidationException) ex).getErrors());

			} else {
				map.put("errors", ex.toString());
			}

			if (ex instanceof NoneLoginException || ex instanceof AuthenticationException) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			}else{
			    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}

			HttpUtils.outJson(map, request, response);

			log.error("Exception：", ex);
		}

	}

    @Override
    public int getOrder() {
        return 0;
    }

}
