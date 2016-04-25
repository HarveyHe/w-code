package com.harvey.common.core.dao.query;

import java.io.Reader;
import java.io.StringReader;

import org.beetl.core.Resource;
import org.beetl.core.ResourceLoader;

public class QueryTemplateResource extends Resource {

    private String query;
    public QueryTemplateResource(String queryName,String query, ResourceLoader loader) {
        super(queryName, loader);
        this.query = query;
    }

    @Override
    public Reader openReader() {
        return new StringReader(query);
    }

    @Override
    public boolean isModified() {
        return false;
    }



}
