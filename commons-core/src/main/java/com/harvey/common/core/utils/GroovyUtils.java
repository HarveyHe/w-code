package com.harvey.common.core.utils;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import org.codehaus.groovy.runtime.InvokerHelper;

public class GroovyUtils {
    private static final GroovyShell groovyShell = new GroovyShell(GroovyShell.class.getClassLoader());

    public static Script parseScript(Reader reader) {
        Script script = groovyShell.parse(reader);
        return script;
    }

    public static Script parseScript(String scriptText) {
        return parseScript(new StringReader(scriptText));
    }

    public static Object invoke(Script script, Map model) {
        Binding binding = new Binding(model);
        Script scriptObject = InvokerHelper.createScript(script.getClass(), binding);
        return scriptObject.run();
    }
}
