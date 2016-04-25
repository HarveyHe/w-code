package com.harvey.common.core.beetl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.beetl.core.GroupTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DefaultInitializing implements TemplateInitializing,ApplicationContextAware {

    private ApplicationContext applicationContext;
    
    @Override
    public void onInitial(GroupTemplate groupTemplate) {
        
        groupTemplate.getConf().setDirectByteOutput(true);
        groupTemplate.getConf().setCharset("utf-8");
        groupTemplate.getConf().setErrorHandlerClass(com.harvey.common.core.beetl.ParseErrorHandler.class.getName());
        groupTemplate.setErrorHandler(new com.harvey.common.core.beetl.ParseErrorHandler());
        
        Map<String,Object> vars = groupTemplate.getSharedVars();
        if(vars == null){
            vars = new HashMap<String,Object>();
            groupTemplate.setSharedVars(vars);
        }
        
        if(applicationContext != null){
            
            vars.put("applicationContext", applicationContext);
            
            Collection<BeetlFunction> funcs = applicationContext.getBeansOfType(BeetlFunction.class).values();
            for(BeetlFunction func:funcs){
                groupTemplate.registerFunction(func.getName(), func);
            }
            
            Collection<BeetlTag> tags = applicationContext.getBeansOfType(BeetlTag.class).values();
            for(BeetlTag tag : tags){
                groupTemplate.registerTagFactory(tag.getName(), new BeetlTagFactory(tag));
            }
        }
    }
    
    public ApplicationContext getApplicationContext(){
        return applicationContext;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
