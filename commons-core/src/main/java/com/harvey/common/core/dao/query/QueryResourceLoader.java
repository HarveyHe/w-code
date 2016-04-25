package com.harvey.common.core.dao.query;

import org.beetl.core.GroupTemplate;
import org.beetl.core.Resource;
import org.beetl.core.ResourceLoader;
import org.hibernate.SessionFactory;

import com.harvey.common.core.dao.utils.QueryUtils;

public class QueryResourceLoader implements ResourceLoader {

    private SessionFactory sessionFactory;
    
    public QueryResourceLoader(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public void close() {

    }

    @Override
    public boolean exist(String queryName) {
        return false;
    }

    @Override
    public Resource getResource(String queryName) {
        String query = QueryUtils.getNamedSql(sessionFactory, queryName);
        return new QueryTemplateResource(queryName,query,this);
    }

    @Override
    public String getResourceId(Resource query, String queryName) {
        return queryName;
    }

    @Override
    public void init(GroupTemplate groupTemplate) {

    }

    @Override
    public boolean isModified(Resource query) {
        return false;
    }

}
