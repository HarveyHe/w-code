package com.harvey.common.core.dao.query;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.hibernate.SessionFactory;

import com.harvey.common.core.beetl.BeetlTemplate;

public class QueryTemplate extends BeetlTemplate {
    private static final Map<SessionFactory, GroupTemplate> groupTemplateCache = new ConcurrentHashMap<SessionFactory, GroupTemplate>();

    public QueryTemplate(SessionFactory sessionFactory) throws IOException {
        super(new QueryResourceLoader(sessionFactory), Configuration.defaultConfiguration());
    }

    public static GroupTemplate getQueryTemplate(SessionFactory sessionFactory) {
        GroupTemplate template = groupTemplateCache.get(sessionFactory);
        if (template == null) {
            synchronized (groupTemplateCache) {
                template = groupTemplateCache.get(sessionFactory);
                if (template == null) {
                    try {
                        template = new QueryTemplate(sessionFactory);
                        groupTemplateCache.put(sessionFactory, template);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return template;
    }

}
