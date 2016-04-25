package com.harvey.common.core.spring.security.tokenaccess.provider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

import com.harvey.common.core.context.Context;
import com.harvey.common.core.model.UserBaseModel;
import com.harvey.common.core.spring.security.tokenaccess.AccessAuthenticationToken;
import com.harvey.common.core.spring.security.tokenaccess.exception.UnauthorizedTokenException;
import com.harvey.common.core.spring.security.tokenaccess.service.TokenizationService;
import com.harvey.common.core.spring.security.utils.SecurityUtils;

public class DefaultTokenAuthenticationProvider implements TokenAuthenticationProvider, InitializingBean, ApplicationContextAware {

    private TokenizationService tokenizationService;

    private UserDetailsChecker userChecker;

    private ApplicationContext applicationContext;

    @Override
    public boolean isSupported(HttpServletRequest request, HttpServletResponse response) {
        //return StringUtils.isNotBlank(request.getParameter(parameterName));
        return SecurityUtils.isAnonymous()
                && tokenizationService.isTokenAccess(request, response);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String token = this.tokenizationService.obtainTokenFromRequest(request);
        UserBaseModel user = this.tokenizationService.autoLogin(token,request, response);
        if (user == null) {
            throw new UnauthorizedTokenException("tokenization");
        }
        userChecker.check(user);
        AccessAuthenticationToken accToken = new AccessAuthenticationToken(user,null,request.getRequestURI());
        this.tokenizationService.authenticateToken(user, accToken);
        accToken.setAuthenticated(true);
        return accToken;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.tokenizationService == null) {
            this.tokenizationService = this.applicationContext.getBean(TokenizationService.class);
        }

        if (userChecker == null) {
            userChecker = new DefaultPreAuthenticationChecks();
        }
    }

    public TokenizationService getTokenizationService() {
        return tokenizationService;
    }

    public void setTokenizationService(TokenizationService tokenizationService) {
        this.tokenizationService = tokenizationService;
    }

    public UserDetailsChecker getUserChecker() {
        return userChecker;
    }

    public void setUserChecker(UserDetailsChecker userChecker) {
        this.userChecker = userChecker;
    }

    private class DefaultPreAuthenticationChecks implements UserDetailsChecker {
        public void check(UserDetails user) {
            if (!user.isAccountNonLocked()) {
                throw new LockedException("User account is locked");
            }

            if (!user.isEnabled()) {

                throw new DisabledException("User is disabled");
            }

            if (!user.isAccountNonExpired()) {

                throw new AccountExpiredException("User account has expired");
            }

            if (!user.isCredentialsNonExpired()) {
                throw new CredentialsExpiredException("User credentials have expired");
            }
        }
    }

    @Override
    public int getOrder() {
        return 100;
    }

}
