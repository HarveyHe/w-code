package com.harvey.common.core.i18n;

public interface ResourceBundle {
    String getBundleName();
    String getString(String key, Object... args);
    String getStringDef(String key,String defVal,Object... args);
}
