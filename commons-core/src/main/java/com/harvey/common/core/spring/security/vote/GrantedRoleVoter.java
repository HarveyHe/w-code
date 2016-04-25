package com.harvey.common.core.spring.security.vote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.harvey.common.core.model.UserBaseModel;
import com.harvey.common.core.spring.security.RoleConfigAttribute;

public class GrantedRoleVoter implements AccessDecisionVoter<Object> {

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return attribute instanceof RoleConfigAttribute;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        int result = ACCESS_ABSTAIN;
        Collection<? extends GrantedAuthority> authorities = extractAuthorities(authentication);
        for (ConfigAttribute attribute : attributes) {
            if (this.supports(attribute)) {
                result = ACCESS_DENIED;
                for (GrantedAuthority authority : authorities) {
                    if (attribute.getAttribute().equals(authority.getAuthority())) {
                        return ACCESS_GRANTED;
                    }
                }
            }
        }

        return result;
    }

    Collection<? extends GrantedAuthority> extractAuthorities(Authentication authentication) {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        if(authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()){
            authorities.addAll(authentication.getAuthorities());
        }
        if(authentication.getPrincipal() instanceof UserBaseModel){
            UserBaseModel user = (UserBaseModel)authentication.getPrincipal();
            if(user.getAuthorities() != null && !user.getAuthorities().isEmpty()){
                authorities.addAll(user.getAuthorities());
            }
        }
        return authorities;
    }
}
