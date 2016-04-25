package com.harvey.common.core.utils;

import groovy.lang.Binding;
import groovy.lang.Script;

import java.util.Map;

public class GroovyScript {
    
    private String scriptText;

    private Script script;
    
    private Map<String,Object> binding;

    public GroovyScript(String scriptText) {
        this(scriptText,null);
    }
    
    public GroovyScript(String scriptText,Map<String,Object> binding) {
        super();
        this.binding = binding;
        this.setScriptText(scriptText);
    }

    public String getScriptText() {
        return scriptText;
    }

    public void setScriptText(String scriptText) {
        if (scriptText != null && !scriptText.equals(this.scriptText)) {
            synchronized (this) {
                this.script = null;
                this.script = GroovyUtils.parseScript(scriptText);
                if(this.binding != null){
                    this.script.setBinding(new Binding(this.binding));
                }
                this.scriptText = scriptText;
            }
        }
    }

    public Script getScript() {
        synchronized (this) {
            return script;
        }
    }

    public Object invoke(Map map) {
        return GroovyUtils.invoke(this.getScript(), map);
    }
    
}
