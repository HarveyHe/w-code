package com.harvey.common.core.utils;

import java.util.Map;

import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerUtils;
import org.springframework.core.io.Resource;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;

public class EhcacheUtils {
    public static Ehcache getCache(String cacheName,Resource configResource){
        Ehcache cache = null;
        CacheManager cacheManager = CacheManager.getInstance();
        if(cacheName != null && cacheManager.cacheExists(cacheName)){
            cache = cacheManager.getEhcache(cacheName);
        } 
        if(cache == null && configResource != null){
            Configuration config = EhCacheManagerUtils.parseConfiguration(configResource);
            if(config.getName() != null){
                cacheManager = CacheManager.getCacheManager(config.getName());
            }
            Map<String, CacheConfiguration> cacheConfigMap = config.getCacheConfigurations();
            CacheConfiguration cacheConfig = null;
            if(cacheName != null && cacheConfigMap.containsKey(cacheName)){
                cacheConfig = cacheConfigMap.get(cacheName);
            }else if(cacheConfigMap.size() == 1){
                cacheConfig = cacheConfigMap.entrySet().iterator().next().getValue();
            }
            if(cacheConfig != null){
                cacheConfig.setupFor(cacheManager);
                cache = cacheManager.getEhcache(cacheName);
            }
        }
        if(cache == null){
            EhCacheFactoryBean factory = new EhCacheFactoryBean();
            if(cacheName != null){
                factory.setName(cacheName);
            }else{
                factory.setBeanName("cache :"+factory.hashCode());
            }
            cache = factory.getObject();
        }
        return cache;
    }
}
