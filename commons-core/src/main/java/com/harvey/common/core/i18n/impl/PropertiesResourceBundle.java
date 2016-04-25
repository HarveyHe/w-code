package com.harvey.common.core.i18n.impl;

import java.util.Properties;

import com.harvey.common.core.i18n.ResourceBundle;

public class PropertiesResourceBundle implements ResourceBundle {

    protected Properties properties;
    private String bundleName;

    public PropertiesResourceBundle(Properties properties, String bundleName) {
        this.properties = properties;
        this.bundleName = bundleName;
    }

    @Override
    public String getBundleName() {
        return bundleName;
    }

    @Override
    public String getString(String key, Object... args) {
        String val = properties.getProperty(key);
        if (args.length > 0) {
            if (val == null) {
                if (args[0] != null) {
                    val = args[0].toString();
                    properties.setProperty(key, val);
                }
            } else {
                val = String.format(val, args);
            }
        }
        if (val == null) {
            val = key;
        }
        return val;
    }

    @Override
    public String getStringDef(String key, String defVal, Object... args) {
        String val = properties.getProperty(key);
        if (val == null) {
            val = defVal;
            properties.setProperty(key, val);
        }
        if (val == null) {
            val = key;
        }
        return args.length > 0 ? String.format(val, args) : val;
    }
    
    public Properties getProperties(){
        return this.properties;
    }

}
