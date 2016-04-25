package com.harvey.common.core.spring.converter;

import java.text.ParseException;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

import com.harvey.common.core.utils.DateUtils;

public class DateTimeConverter implements Converter<String, Date> {

    @Override
    public Date convert(String source) {
        try {
            return DateUtils.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
