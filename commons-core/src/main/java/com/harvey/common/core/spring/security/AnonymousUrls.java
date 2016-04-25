package com.harvey.common.core.spring.security;

import java.util.Collections;
import java.util.List;

public class AnonymousUrls {
    private List<String> urlPatterns = Collections.emptyList();

    public List<String> getUrlPatterns() {
        return this.urlPatterns;
    }

    public void setUrlPatterns(List<String> urlPatterns) {
        this.urlPatterns = urlPatterns;
    }
}
