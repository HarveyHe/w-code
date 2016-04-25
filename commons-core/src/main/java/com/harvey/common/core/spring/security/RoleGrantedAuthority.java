package com.harvey.common.core.spring.security;

import org.springframework.security.core.GrantedAuthority;

public class RoleGrantedAuthority implements GrantedAuthority {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private String role;
    
    public RoleGrantedAuthority(String role){
        this.role = role;
    }
    
    @Override
    public String getAuthority() {
        return role;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((role == null) ? 0 : role.hashCode());
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
        RoleGrantedAuthority other = (RoleGrantedAuthority) obj;
        return role != null && role.equals(other.role);
    }

    @Override
    public String toString() {
        return "[role=" + role + "]";
    }

}
