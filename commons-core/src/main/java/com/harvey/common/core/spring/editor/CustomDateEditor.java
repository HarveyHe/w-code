package com.harvey.common.core.spring.editor;

import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.util.Date;

import com.harvey.common.core.utils.DateUtils;

public class CustomDateEditor extends PropertyEditorSupport {
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.trim().length() == 0) {
            setValue(null);
            return;
        }
        try {
            setValue(DateUtils.parse(text));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getAsText() {
        Date date = (Date) getValue();
        if (date == null) {
            return "";
        } else {
            return DateUtils.formatDateTime(date);
        }
    }
}
