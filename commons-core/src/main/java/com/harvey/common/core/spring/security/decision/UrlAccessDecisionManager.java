package com.harvey.common.core.spring.security.decision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;

public class UrlAccessDecisionManager implements AccessDecisionManager, InitializingBean, ApplicationContextAware {

    private Collection<AccessDecisionVoter> decisionVoters;
    private ApplicationContext applicationContext;

    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {
        int result = 10;
        for (AccessDecisionVoter voter : getDecisionVoters()) {
            result = voter.vote(authentication, object, configAttributes);
            if (result != 0) {
                if (result == -1) {
                    throw new AccessDeniedException("Access is denied");
                }
                if (result == 1) {
                    break;
                }
            }
        }
        if ((result == 0) && (configAttributes.size() > 0)) {
            throw new AccessDeniedException("Access is denied");
        }
    }

    public Collection<AccessDecisionVoter> getDecisionVoters() {
        return decisionVoters;
    }

    public void setDecisionVoters(List<AccessDecisionVoter> decisionVoters) {
        this.decisionVoters = decisionVoters;
    }

    public boolean supports(ConfigAttribute attribute) {
        for (AccessDecisionVoter<Object> voter : this.decisionVoters) {
            if (voter.supports(attribute)) {
                return true;
            }
        }

        return false;
    }

    public boolean supports(Class<?> clazz) {
        for (AccessDecisionVoter<Object> voter : this.decisionVoters) {
            if (!voter.supports(clazz)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(decisionVoters == null || decisionVoters.size() == 0){
            decisionVoters = this.applicationContext.getBeansOfType(AccessDecisionVoter.class).values();
            if(decisionVoters == null || decisionVoters.size() == 0){
                decisionVoters = new ArrayList<AccessDecisionVoter>();
                decisionVoters.add(new AuthenticatedVoter());
            }
        }
    }
}