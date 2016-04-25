package com.harvey.common.core.spring.security.tokenaccess;


import org.springframework.security.authentication.AbstractAuthenticationToken;


public class AccessAuthenticationToken extends AbstractAuthenticationToken {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Object principal;
    private Object credentials;
    private String url;
    private String accessToken;
    
    public AccessAuthenticationToken(Object principal, Object credentials,String url) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        this.url = url;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setCredentials(Object credentials) {
        this.credentials = credentials;
    }

}
