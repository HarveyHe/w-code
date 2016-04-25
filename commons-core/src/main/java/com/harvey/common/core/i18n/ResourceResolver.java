package com.harvey.common.core.i18n;

import java.util.Locale;
import java.util.Map;

public interface ResourceResolver {
    String getValue(String bundleName,String key,String defValue,Locale locale,Object ... args);
    Map<String, String> resolver(String bundleName,Locale locale);
}
