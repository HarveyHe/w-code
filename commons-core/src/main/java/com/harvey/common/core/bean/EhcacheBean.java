package com.harvey.common.core.bean;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import com.harvey.common.core.utils.EhcacheUtils;

public class EhcacheBean implements InitializingBean {
    private Ehcache cache;
    private String cacheName;
    private Resource cacheConfig;
    protected DataProvider dataProvider;
    protected boolean loaded=false;

    protected void loadAll(){
        if( !loaded && dataProvider != null){
            Map<Object,Object> data = dataProvider.getAllToCache();
            this.cache(data);
            loaded = true;
        }
    }
    
    protected Object load(Object key){
        if(dataProvider != null){
            Object value = dataProvider.getItemToCache(key);
            this.cache(key, value);
            return value;
        }
        return null;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.cache == null) {
            this.cache = EhcacheUtils.getCache(cacheName, cacheConfig);
        }
        if(dataProvider != null && dataProvider.mode() == LoadMode.AllOnInit ){
            loadAll();
        }
    }

    public DataProvider getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @SuppressWarnings("unchecked")
    public <T> T cache(Object key) {
        loadAll();
        Element el = this.cache.get(key);
        if (el == null) {
            return (T) this.load(key);
        }
        return (T) el.getObjectValue();
    }

    public void cache(Object key, Object value) {
        Element el = new Element(key, value);
        this.cache.put(el);
    }

    public void cache(Map<?, ?> elements) {
        for (Entry<?, ?> entry : elements.entrySet()) {
            this.cache(entry.getKey(), entry.getValue());
        }
    }

    public void reload(){
        this.cache.removeAll();
        this.loaded = false;
        this.loadAll();
    }
    
    public MultiKey genMultiKey(Object... keys) {
        MultiKey key = new MultiKey(keys);
        return key;
    }

    public Ehcache getCache() {
        return cache;
    }

    public void setCache(Ehcache cache) {
        this.cache = cache;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public void setCacheConfig(Resource cacheConfig) {
        this.cacheConfig = cacheConfig;
    }

    public interface DataProvider {
        
        LoadMode mode();
        
        Map<Object,Object> getAllToCache();
        
        Object getItemToCache(Object key);
    }

    public enum LoadMode {
        /**
         * 按需加载至缓存
         */
        ByNeed,
        
        /**
         * 初始化时加载所有数据至缓存
         */
        AllOnInit, 
        
        /**
         * 延迟加载所有数据至缓存
         */
        AllByLazy
    }
    
    public static class SimpleCacheBean extends EhcacheBean {
        private Map<Object,Object> cache = new ConcurrentHashMap<Object, Object>();

        @Override
        public void afterPropertiesSet() throws Exception {
            if(dataProvider != null && dataProvider.mode() == LoadMode.AllOnInit ){
                loadAll();
            }
        }

        @Override
        public <T> T cache(Object key) {
            this.loadAll();
            T val = (T) cache.get(key);
            if(val == null){
                val = (T) this.load(key);
            }
            return val;
        }

        @Override
        public void cache(Object key, Object value) {
            this.cache.put(key, value);
        }

        @Override
        public void cache(Map<?, ?> elements) {
            this.cache.putAll(elements);
        }

        @Override
        public void reload() {
            this.cache.clear();
            this.loaded = false;
            this.loadAll();
        }
        
    }
}
