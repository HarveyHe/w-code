package com.harvey.common.core.spring.security.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.util.AntPathMatcher;

import com.harvey.common.core.context.Context;
import com.harvey.common.core.model.UserBaseModel;
import com.harvey.common.core.spring.security.AnonymousUrl;
import com.harvey.common.core.spring.security.AnonymousUrls;

@Deprecated
public class UserUrlMetadataSource implements FilterInvocationSecurityMetadataSource, InitializingBean, ApplicationContextAware {

    private static final Collection<ConfigAttribute> AUTHENTICATED_FULLY;
    static {
        AUTHENTICATED_FULLY = new ArrayList<ConfigAttribute>();
        AUTHENTICATED_FULLY.add(new SecurityConfig(AuthenticatedVoter.IS_AUTHENTICATED_FULLY));
    }

    private ApplicationContext applicationContext;
    private Map<String, ConfigAttribute> anonymousUrlMetadata = new HashMap<String, ConfigAttribute>();
    private AntPathMatcher matcher = new AntPathMatcher();
    private Set<String> commonUrls;
    private Set<String> anonymousUrls;

    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        String url = null;
        if ((object instanceof FilterInvocation)) {
            HttpServletRequest request = ((FilterInvocation) object).getRequest();
            url = getRequestPath(request);
        } else {
            url = (String) object;
        }
        Collection<ConfigAttribute> safeUrlAttributes = getAnonymousUrlAttributes(url);
        if (safeUrlAttributes != null) {
            return safeUrlAttributes;
        }
        Collection<ConfigAttribute> userAttributes = this.getUserUrlAttributes(url);
        if (userAttributes != null && userAttributes.size() > 0) {
            return userAttributes;
        }
        throw new AccessDeniedException("Access denied");
    }

    private Map<String, Collection<ConfigAttribute>> getConfigAttributesMap(UserBaseModel user) {
        @SuppressWarnings("unchecked")
        Map<String, Collection<ConfigAttribute>> attributes = (Map<String, Collection<ConfigAttribute>>) user.getAttribute().get(this.getClass().getName());
        if (attributes == null) {
            attributes = new HashMap<String, Collection<ConfigAttribute>>();
            user.getAttribute().put(this.getClass().getName(), attributes);
        }
        return attributes;
    }

    private Collection<ConfigAttribute> getUserUrlAttributes(String url) {
        UserBaseModel user = Context.getCurrentUser();
        if (user == null) {
            return Collections.emptyList();
        }
        Map<String, Collection<ConfigAttribute>> attributesMap = getConfigAttributesMap(user);
        Collection<ConfigAttribute> attributes = attributesMap.get(url);
        if (attributes == null) {
            attributes = this.buildUserUrlConfigAttributes(user, url);
            attributesMap.put(url, attributes);
        }
        return attributes;
    }

    private Collection<ConfigAttribute> getAnonymousUrlAttributes(String url) {
        Collection<ConfigAttribute> attributes = null;
        for (String patternUrl : this.anonymousUrlMetadata.keySet()) {
            if (this.matcher.match(patternUrl, url)) {
                attributes = new ArrayList<ConfigAttribute>();
                attributes.add(this.anonymousUrlMetadata.get(patternUrl));
                break;
            }
        }
        return attributes;
    }

    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return Collections.emptyList();
    }

    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }

    private String getRequestPath(HttpServletRequest request) {
        String url = request.getServletPath();
        if (request.getPathInfo() != null) {
            url = url + request.getPathInfo();
        }
        if (url.length() == 1 && url.charAt(0) == '/') {
            url = "/index.jsp";
        }
        return url;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.anonymousUrls == null) {
            this.anonymousUrls = new HashSet<String>();
        }
        Collection<AnonymousUrl> safeUrls = applicationContext.getBeansOfType(AnonymousUrl.class).values();
        for (AnonymousUrl url : safeUrls) {
            this.anonymousUrls.add(url.getUrlPattern());
        }
        Collection<AnonymousUrls> safeUrlCollections = applicationContext.getBeansOfType(AnonymousUrls.class).values();
        for (AnonymousUrls urls : safeUrlCollections) {
            this.anonymousUrls.addAll(urls.getUrlPatterns());
        }
        buildSafeUrlConfigAttributes();
    }

    private Boolean isMatch(Collection<String> urls, String url) {
        for (String u : urls) {
            if (this.matcher.match(u, url)) {
                return true;
            }
        }
        return false;
    }

    private Collection<ConfigAttribute> buildUserUrlConfigAttributes(UserBaseModel user, String url) {
        Collection<ConfigAttribute> attributes = Collections.emptyList();
        if (this.commonUrls != null && this.isMatch(this.commonUrls, url)) {
            attributes = AUTHENTICATED_FULLY;
        } else {
            if (user.getAuthorities() == null) {
                return attributes;
            }
            for (GrantedAuthority authority : user.getAuthorities()) {
                if (StringUtils.isEmpty(authority.getAuthority())) {
                    continue;
                }
                String url1 = authority.getAuthority();
                if (url1.charAt(0) != '/') {
                    url1 = '/' + url1;
                }
                if (this.matcher.match(url1,url)) {
                    attributes = AUTHENTICATED_FULLY;
                    break;
                }
            }
        }
        return attributes;
    }

    public void buildSafeUrlConfigAttributes() {
        SecurityConfig attribute = new SecurityConfig(AuthenticatedVoter.IS_AUTHENTICATED_ANONYMOUSLY);
        for (String url : getAnonymousUrls()) {
            this.anonymousUrlMetadata.put(url, attribute);
        }
    }

    public Set<String> getCommonUrls() {
        return commonUrls;
    }

    public void setCommonUrls(Set<String> commonUrls) {
        this.commonUrls = commonUrls;
    }

    public Set<String> getAnonymousUrls() {
        return anonymousUrls;
    }

    public void setAnonymousUrls(Set<String> anonymousUrls) {
        this.anonymousUrls = anonymousUrls;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
