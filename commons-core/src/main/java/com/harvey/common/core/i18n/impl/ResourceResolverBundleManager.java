package com.harvey.common.core.i18n.impl;

import java.util.Locale;

import com.harvey.common.core.i18n.ResourceBundle;
import com.harvey.common.core.i18n.ResourceResolver;

public class ResourceResolverBundleManager extends AbstractResourceBundleManager {

    private ResourceResolver resourceResolver;
    
    public ResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    public void setResourceResolver(ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    @Override
    ResourceBundle doGetBundle(String bundleName, Locale locale) throws Exception {
        return new ResourceResolverBundle(resourceResolver, bundleName, locale);
    }

}
