package com.harvey.common.core.spring.security.utils;

import org.springframework.security.authentication.AnonymousAuthenticationToken;

import com.harvey.common.core.context.Context;

public class SecurityUtils {
    public static boolean isAnonymous() {
        return Context.getAuthentication() == null || AnonymousAuthenticationToken.class.isAssignableFrom(Context.getAuthentication().getClass());
    }
}
