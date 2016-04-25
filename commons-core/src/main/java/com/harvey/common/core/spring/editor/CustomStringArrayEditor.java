package com.harvey.common.core.spring.editor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.lang3.StringUtils;


public class CustomStringArrayEditor extends PropertyEditorSupport {

    public void setAsText(String text) throws IllegalArgumentException {
        setValue(StringUtils.split(text, ','));
    }

    public String getAsText() {
        return StringUtils.join((String[])getValue(), ',');
    }

}
