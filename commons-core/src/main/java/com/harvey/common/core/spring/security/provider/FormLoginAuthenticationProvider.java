package com.harvey.common.core.spring.security.provider;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.harvey.common.core.model.UserBaseModel;
import com.harvey.common.core.service.UserBaseService;

public class FormLoginAuthenticationProvider extends DaoAuthenticationProvider implements ApplicationContextAware {
    private UserBaseService userManager;
    private ApplicationContext applicationContext;

    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        UserBaseModel user = (UserBaseModel) userDetails;
        try {
            this.getUserManager().authenticate(user, authentication);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AuthenticationServiceException(ex.getMessage());
        }
    }

    public UserBaseService getUserManager() {
        return userManager;
    }

    public void setUserManager(UserBaseService userManager) {
        this.userManager = userManager;
    }

    @Override
    protected void doAfterPropertiesSet() throws Exception {
        if(this.getUserDetailsService() == null){
            this.setUserDetailsService(applicationContext.getBean(UserDetailsService.class));
        }
        if(this.getUserManager() == null){
            this.userManager = applicationContext.getBean(UserBaseService.class);
        }
        super.doAfterPropertiesSet();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}