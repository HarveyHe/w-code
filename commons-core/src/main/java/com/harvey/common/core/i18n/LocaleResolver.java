package com.harvey.common.core.i18n;

import java.util.Locale;

public interface LocaleResolver {
    public Locale resolveLocale() throws Exception;
}
