package com.harvey.common.core.spring.security;

import org.springframework.security.core.GrantedAuthority;

public class UrlGrantedAuthority implements GrantedAuthority {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private String url;
    
    public UrlGrantedAuthority(String url){
        this.url = url;
    }
    
    @Override
    public String getAuthority() {
        return url;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (obj == null || getClass() != obj.getClass()){
            return false;
        }
        UrlGrantedAuthority other = (UrlGrantedAuthority) obj;
        return url != null && url.equals(other.url);
    }

    @Override
    public String toString() {
        return "[url=" + url + "]";
    }

}
