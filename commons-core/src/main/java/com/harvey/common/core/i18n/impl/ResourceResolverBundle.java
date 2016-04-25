package com.harvey.common.core.i18n.impl;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import com.harvey.common.core.i18n.ResourceResolver;

public class ResourceResolverBundle extends PropertiesResourceBundle {

    private ResourceResolver resourceResolver;
    private Locale locale;

    public ResourceResolverBundle(ResourceResolver resourceHandler, String bundleName, Locale locale) {
        super(new Properties(), bundleName);
        this.resourceResolver = resourceHandler;
        this.locale = locale;
    }

    private void initBundle(String key, String defVal) {
        if(!properties.containsKey(key)){
            synchronized (properties) {
                if (properties.isEmpty()) {
                    Map<String, String> resources = resourceResolver.resolver(this.getBundleName(), locale);
                    properties.putAll(resources);
                }
                if(!properties.containsKey(key)){
                   String val = resourceResolver.getValue(this.getBundleName(), key, defVal, locale);
                   properties.setProperty(key, val);
                }
            }
        }
    }

    @Override
    public String getString(String key, Object... args) {
        initBundle(key, args.length > 0 && args[0] != null ? args[0].toString() : null);
        return super.getString(key, args);
    }

    @Override
    public String getStringDef(String key, String defVal, Object... args) {
        initBundle(key,defVal);
        return super.getStringDef(key, defVal, args);
    }

}
