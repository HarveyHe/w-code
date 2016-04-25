package com.harvey.common.core.i18n;


public interface ResourceBundleManager {
    
    ResourceBundle getBundle(String bundleName) throws Exception;
    
    LocaleResolver getLocaleResolver();
}
