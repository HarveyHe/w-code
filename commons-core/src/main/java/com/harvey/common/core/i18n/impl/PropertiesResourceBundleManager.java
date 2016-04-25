package com.harvey.common.core.i18n.impl;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.springframework.core.io.Resource;

import com.harvey.common.core.config.ConfigLoader;
import com.harvey.common.core.i18n.ResourceBundle;
import com.harvey.common.core.utils.ResourceUtils;

public class PropertiesResourceBundleManager extends AbstractResourceBundleManager {

    private String basePackages;

    public String getBasePackages() {
        return basePackages;
    }

    public void setBasePackages(String basePackages) {
        this.basePackages = basePackages;
    }

    @Override
    ResourceBundle doGetBundle(String bundleName, Locale locale) throws Exception {
        String pattern = "/**/" + bundleName + "_" + locale.toString() + ".properties";
        List<Resource> resources = ResourceUtils.getResources(basePackages, pattern);
        Properties properties = new Properties();
        for (Resource resource : resources) {
            InputStream is = resource.getInputStream();
            ConfigLoader.loadProperties(properties, is);
        }
        return new PropertiesResourceBundle(properties, bundleName);
    }

}
