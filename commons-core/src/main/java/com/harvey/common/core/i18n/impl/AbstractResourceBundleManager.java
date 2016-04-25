package com.harvey.common.core.i18n.impl;

import java.util.Locale;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.harvey.common.core.bean.EhcacheBean;
import com.harvey.common.core.i18n.LocaleResolver;
import com.harvey.common.core.i18n.ResourceBundle;
import com.harvey.common.core.i18n.ResourceBundleManager;

public abstract class AbstractResourceBundleManager implements ResourceBundleManager,InitializingBean {

    private EhcacheBean ehcacheBean;
    private LocaleResolver localeResolver;
    
    public LocaleResolver getLocaleResolver() {
        return localeResolver;
    }

    public void setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    public EhcacheBean getEhcacheBean() {
        return ehcacheBean;
    }

    public void setEhcacheBean(EhcacheBean ehcacheBean) {
        this.ehcacheBean = ehcacheBean;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(ehcacheBean, "ehcacheBean not allow null");
        Assert.notNull(localeResolver, "localeResolver not allow null");
    }    
    
    @Override
    public ResourceBundle getBundle(String bundleName) throws Exception {
        Locale locale = getLocaleResolver().resolveLocale();
        Object cacheKey = ehcacheBean.genMultiKey(bundleName,locale);
        synchronized (ehcacheBean) {
            ResourceBundle bundle = ehcacheBean.cache(cacheKey);
            if (bundle == null) {
                bundle = doGetBundle(bundleName, locale);
                if(bundle == null){
                    return bundle;
                }
                ehcacheBean.cache(cacheKey,bundle);
            }
            return bundle;
        }
    }

    abstract ResourceBundle doGetBundle(String bundleName, Locale locale) throws Exception;
    
}
