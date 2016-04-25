package com.harvey.common.core.i18n.impl;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import com.harvey.common.core.context.Context;
import com.harvey.common.core.i18n.LocaleResolver;

public class DefaultLocaleResolver implements LocaleResolver,InitializingBean {

    private static final String Defualt_Lang_Cookie_Name = "lang";

    private CookieLocaleResolver cookieLocaleResolver;
    private String cookieName;

    public CookieLocaleResolver getCookieLocaleResolver() {
        return cookieLocaleResolver;
    }

    public void setCookieLocaleResolver(CookieLocaleResolver cookieLocaleResolver) {
        this.cookieLocaleResolver = cookieLocaleResolver;
    }

    @Override
    public Locale resolveLocale() throws Exception {
        HttpServletRequest request = Context.getRequest();
        if(request == null){
            return Locale.getDefault();
        }
        return cookieLocaleResolver.resolveLocale(request);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isEmpty(cookieName)) {
            cookieName = Defualt_Lang_Cookie_Name;
        }
        if (this.cookieLocaleResolver == null) {
            this.cookieLocaleResolver = new CookieLocaleResolver();
        }
        this.cookieLocaleResolver.setCookieName(cookieName);
    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }


}
