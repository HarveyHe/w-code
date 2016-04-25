package com.harvey.common.core.spring.mvc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.WebRequest;

public class WebBindingInitializerComposite implements WebBindingInitializer {

    private List<WebBindingInitializer> webBindingInitializers = new ArrayList<WebBindingInitializer>();
    
    
    @Override
    public void initBinder(WebDataBinder binder, WebRequest request) {
        for(WebBindingInitializer webBindingInitializer : this.webBindingInitializers){
            webBindingInitializer.initBinder(binder, request);
        }
    }


    public List<WebBindingInitializer> getWebBindingInitializers() {
        return webBindingInitializers;
    }

    public void addWebBindingInitializer(WebBindingInitializer webBindingInitializer){
        if(webBindingInitializer != null){
            this.webBindingInitializers.add(webBindingInitializer);
        }
    }

    public void addWebBindingInitializers(Collection<WebBindingInitializer> webBindingInitializers) {
        if(webBindingInitializers != null){
            this.webBindingInitializers.addAll(webBindingInitializers);   
        }
    }


    public void setWebBindingInitializers(List<WebBindingInitializer> webBindingInitializers) {
        this.addWebBindingInitializers(webBindingInitializers);
    }

}
