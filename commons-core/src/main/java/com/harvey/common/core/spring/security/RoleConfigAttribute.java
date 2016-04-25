package com.harvey.common.core.spring.security;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.util.Assert;

public class RoleConfigAttribute implements ConfigAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private String role;
    
    public RoleConfigAttribute(String role){
        Assert.hasText(role, "You must provide a configuration attribute");
        this.role = role;
    }
    
    @Override
    public String getAttribute() {
        return role;
    }

}
