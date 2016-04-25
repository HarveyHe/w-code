package com.harvey.common.core.spring.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

public class EmptyRequestCacheImpl implements RequestCache {

    @Override
    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    public SavedRequest getRequest(HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

    @Override
    public HttpServletRequest getMatchingRequest(HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

    @Override
    public void removeRequest(HttpServletRequest request, HttpServletResponse response) {

    }

}
