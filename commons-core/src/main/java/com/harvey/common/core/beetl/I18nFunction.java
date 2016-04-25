package com.harvey.common.core.beetl;

import java.util.Locale;

import org.beetl.core.Context;

import com.harvey.common.core.i18n.LocaleResolver;

public class I18nFunction implements BeetlFunction {

    private LocaleResolver localeResolver;
    private String name = "lang";
    
    @Override
    public Object call(Object[] args, Context ctx) {
        String locale = this.getLocale();
        return doComparer(locale,args);
    }
    private static Object doComparer(String locale,Object...args){
        int i = 0;
        while (true)
        {
            if (locale.equals(args[i]) ||
                    (args[i] != null && locale.equalsIgnoreCase(args[i].toString())))
            {
                return args[i + 1];
            }
            else
            {
                if (args.length == i + 3)
                {
                    //default
                    return args[i + 2];
                }
                else
                {
                    i = i + 2;
                    continue;
                }
            }
        }        
    }
    
    public static void main(String[] args){
        String locale = Locale.getDefault().toString();
        Object result =  doComparer(locale,"zh_CN","t1","zh_TW","t2","en","t3","t4");
        System.out.println(result);
    }
    
    private String getLocale(){
        try {
            return localeResolver.resolveLocale().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return name;
    }
    
    public void setName(String name){
        this.name = name;
    }

    public LocaleResolver getLocaleResolver() {
        return localeResolver;
    }

    public void setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

}
