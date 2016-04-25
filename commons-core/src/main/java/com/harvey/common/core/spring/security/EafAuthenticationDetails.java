package com.harvey.common.core.spring.security;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class EafAuthenticationDetails extends WebAuthenticationDetails {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Map<String,String> parameters;
    
    public EafAuthenticationDetails(HttpServletRequest request) {
        super(request);
        Enumeration<String> names = request.getParameterNames();
        parameters = new HashMap<String,String>();
        while(names.hasMoreElements()){
            String name = names.nextElement();
            parameters.put(name, request.getParameter(name));
        }
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
    
    

}
