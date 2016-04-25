package com.harvey.common.core.spring.security.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Ehcache;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.util.AntPathMatcher;

import com.harvey.common.core.bean.EhcacheBean;
import com.harvey.common.core.bean.EhcacheBean.SimpleCacheBean;
import com.harvey.common.core.context.Context;
import com.harvey.common.core.model.UserBaseModel;
import com.harvey.common.core.spring.security.AnonymousUrl;
import com.harvey.common.core.spring.security.AnonymousUrls;
import com.harvey.common.core.spring.security.RoleConfigAttribute;
import com.harvey.common.core.spring.security.UrlGrantedAuthority;
import com.harvey.common.core.spring.security.utils.SecurityUtils;

public class RoleUrlMetadataSource implements FilterInvocationSecurityMetadataSource,InitializingBean,ApplicationContextAware {

    public static final String AnonymousUrlsCacheKey = "AnonymousUrls";
    public static final String SafeUrlsCacheKey = "SafeUrls";
    public static final String UrlRolesCacheKey = "UrlRoles";

    private static final Collection<ConfigAttribute> AUTHENTICATED_FULLY = Arrays.asList((ConfigAttribute) new SecurityConfig(AuthenticatedVoter.IS_AUTHENTICATED_FULLY));
    private static final Collection<ConfigAttribute> AUTHENTICATED_ANONYMOUSLY = Arrays.asList((ConfigAttribute) new SecurityConfig(AuthenticatedVoter.IS_AUTHENTICATED_ANONYMOUSLY));
    private static final AntPathMatcher matcher = new AntPathMatcher();

    private EhcacheBean ehcacheBean;
    private ApplicationContext applicationContext;

    Set<String> getAnonymousUrls() {
        Set<String> set = getEhcacheBean().cache(AnonymousUrlsCacheKey);
        return set;
    }

    /**
     * 允许匿名(未登录)访问的Url
     * 
     */
    public void setAnonymousUrls(Set<String> anonymousUrls) {
        this.getEhcacheBean().cache(AnonymousUrlsCacheKey, anonymousUrls);
    }

    /**
     * 合并允许匿名(未登录)访问的Url
     * 
     */
    public void updateAnonymousUrls(Set<String> anonymousUrls) {
        Set<String> set = this.getAnonymousUrls();
        this.getEhcacheBean().cache(AnonymousUrlsCacheKey, mergeSet(anonymousUrls, set));
    }

    public void removeAnonymousUrls(List<String> anonymousUrls) {
        Set<String> set = this.getAnonymousUrls();
        set.removeAll(anonymousUrls);
        this.getEhcacheBean().cache(AnonymousUrlsCacheKey, set);
    }

    /**
     * 登录后即访问的url
     * 
     * @return
     */
    Set<String> getSafeUrls() {
        Set<String> set = this.getEhcacheBean().cache(SafeUrlsCacheKey);
        return set;
    }

    /**
     * 登录后即访问的url
     * 
     */
    public void setSafeUrls(Set<String> safeUrls) {
        this.getEhcacheBean().cache(SafeUrlsCacheKey, safeUrls);
    }

    /**
     * 合并登录后即访问的url
     * 
     */
    public void updateSafeUrls(Set<String> safeUrls) {
        Set<String> set = this.getSafeUrls();
        this.getEhcacheBean().cache(SafeUrlsCacheKey, mergeSet(safeUrls, set));
    }

    public void removeSafeUrls(List<String> safeUrls) {
        Set<String> set = this.getSafeUrls();
        set.removeAll(safeUrls);
        this.getEhcacheBean().cache(SafeUrlsCacheKey, set);
    }

    /**
     * 固定的url角色映射关系
     * 
     * @see RoleUrlMetadataSource
     * @param url
     * @return
     */
    Map<String, Set<String>> getUrlsRoles() {
        Map<String, Set<String>> map = this.getEhcacheBean().cache(UrlRolesCacheKey);
        return map;
    }

    /**
     * url角色映射关系
     * 
     * @see RoleUrlMetadataSource
     * @param url
     * @return
     */
    public void setUrlsRoles(Map<String, Set<String>> urlRoles) {
        this.getEhcacheBean().cache(UrlRolesCacheKey, urlRoles);
    }

    /*
     * 合并url角色映射关系
     */
    public void updateUrlsRoles(Map<String, Set<String>> urlRoles) {
        Map<String, Set<String>> map = mergeMap(this.getUrlsRoles(), urlRoles);
        this.getEhcacheBean().cache(UrlRolesCacheKey, map);
    }
    
    /**
     * 设置url角色映射关系
     * @param url
     * @param roles
     */
    public void setUrlRoles(String url,Set<String> roles){
        Map<String, Set<String>> map = this.getUrlsRoles();
        map.put(url, roles);
        this.getEhcacheBean().cache(UrlRolesCacheKey, map);
    }

    /**
     * 缓存的,可变的url角色映射关系
     * 
     * @return
     */
    public EhcacheBean getEhcacheBean() {
        if (this.ehcacheBean == null) {
            this.ehcacheBean = new SimpleCacheBean();
        }
        return ehcacheBean;
    }

    /**
     * 缓存的,可变的url角色映射关系
     * 
     * @return
     */
    public void setEhcacheBean(EhcacheBean ehcacheBean) {
        this.ehcacheBean = ehcacheBean;
    }

    public void setEhcache(Ehcache cache) {
        if (this.ehcacheBean == null) {
            this.ehcacheBean = new EhcacheBean();
        }
        this.ehcacheBean.setCache(cache);
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        String url;
        if ((object instanceof FilterInvocation)) {
            HttpServletRequest request = ((FilterInvocation) object).getRequest();
            url = getRequestPath(request);
        } else {
            url = (String) object;
        }
        Set<String> urls = this.getAnonymousUrls();
        if (isMatch(url, urls)) {
            return AUTHENTICATED_ANONYMOUSLY;
        }
        if (!SecurityUtils.isAnonymous()) {
            urls = this.getSafeUrls();
            if (isMatch(url, urls)) {
                return AUTHENTICATED_FULLY;
            }
        }
        Map<String, Set<String>> urlRoles = this.getUrlsRoles();
        List<ConfigAttribute> attributes = new ArrayList<ConfigAttribute>();
        matchConfigAttribute(url, attributes, urlRoles);
        if (attributes.size() > 0) {
            return attributes;
        }
        Authentication authentication = Context.getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority instanceof UrlGrantedAuthority && matcher.match(authority.getAuthority(), url)) {
                    return AUTHENTICATED_FULLY;
                }
            }
        }
        UserBaseModel user = Context.getCurrentUser();
        if (user != null && user.getAuthorities() != null) {
            for (GrantedAuthority authority : user.getAuthorities()) {
                if (authority instanceof UrlGrantedAuthority && matcher.match(authority.getAuthority(), url)) {
                    return AUTHENTICATED_FULLY;
                }
            }
        }
        throw new AccessDeniedException("Access denied");
    }

    private static void matchConfigAttribute(String url, List<ConfigAttribute> attributes, Map<String, Set<String>> urlRoles) {
        if (urlRoles == null) {
            return;
        }
        for (Entry<String, Set<String>> entry : urlRoles.entrySet()) {
            if (matcher.match(entry.getKey(), url)) {
                for (String role : entry.getValue()) {
                    attributes.add(new RoleConfigAttribute(role));
                }
            }
        }
    }

    private static boolean isMatch(String url, Collection<String> urls) {
        for (String url_ : urls) {
            if (matcher.match(url_, url)) {
                return true;
            }
        }
        return false;
    }


    private static String getRequestPath(HttpServletRequest request) {
        String url = request.getServletPath();
        if (request.getPathInfo() != null) {
            url = url + request.getPathInfo();
        }
        if (url.length() == 1 && url.charAt(0) == '/') {
            url = "/index.jsp";
        }
        return url;
    }

    private static Map<String, Set<String>> mergeMap(Map<String, Set<String>> map1, Map<String, Set<String>> map2) {
        if (map1 == null && map2 == null) {
            return new HashMap<String, Set<String>>();
        }
        if (map2 == null) {
            return map1;
        }
        if (map1 == null) {
            return map2;
        }
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        for (Entry<String, Set<String>> entry : map1.entrySet()) {
            map.put(entry.getKey(), mergeSet(entry.getValue(), map.get(entry.getKey())));
        }
        for (Entry<String, Set<String>> entry : map2.entrySet()) {
            map.put(entry.getKey(), mergeSet(entry.getValue(), map.get(entry.getKey())));
        }
        return map;
    }

    private static <T> Set<T> mergeSet(Set<T> set1, Set<T> set2) {
        if (set1 == null && set2 == null) {
            return new HashSet<T>();
        }
        if (set2 == null) {
            return set1;
        }
        if (set1 == null) {
            return set2;
        }
        Set<T> set = new HashSet<T>(set1.size() + set2.size());
        set.addAll(set1);
        set.addAll(set2);
        return set;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return Collections.emptyList();
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz) || clazz == String.class;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Set<String> anonymousUrls = new HashSet<String>();
        Collection<AnonymousUrl> safeUrls = applicationContext.getBeansOfType(AnonymousUrl.class).values();
        for (AnonymousUrl url : safeUrls) {
            anonymousUrls.add(url.getUrlPattern());
        }
        Collection<AnonymousUrls> safeUrlCollections = applicationContext.getBeansOfType(AnonymousUrls.class).values();
        for (AnonymousUrls urls : safeUrlCollections) {
            anonymousUrls.addAll(urls.getUrlPatterns());
        }   
        this.updateAnonymousUrls(anonymousUrls);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
