package com.harvey.common.core.service.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.harvey.common.core.dao.UniversalDao;
import com.harvey.common.core.model.BaseModel;
import com.harvey.common.core.model.PagingInfo;
import com.harvey.common.core.repository.BaseRepository;

@Deprecated
public abstract class BaseManagerImpl<TModel extends BaseModel, TKey extends Serializable> implements BaseRepository<TModel, TKey> {

    private final Log log = LogFactory.getLog(getClass());
    private final Class<TModel> modelClass;
    private final Class<TKey> keyClass;

    public BaseManagerImpl(Class<TModel> modelClass, Class<TKey> keyClass) {
        this.modelClass = modelClass;
        this.keyClass = keyClass;
    }

    @Autowired
    private UniversalDao dao;

    @Override
    public TModel get(TKey id) {
        return this.dao.get(modelClass, id);
    }

    @Override
    public List<TModel> getAll() {
        return this.getAll(null,null);
    }

    @Override
    public List<TModel> getAll(String orderBy) {
        return this.getAll(orderBy, null);
    }
    
    @Override
    public List<TModel> getAll(String orderBy,PagingInfo pagingInfo){
        return this.dao.getAll(modelClass, orderBy, pagingInfo);
    }

    @Override
    public List<TModel> getAll(PagingInfo pagingInfo) {
        return this.dao.getAll(modelClass, null, pagingInfo);
    }

    @Override
    public List<TModel> findByExample(TModel example) {
        return this.dao.findByExample(example);
    }

    @Override
    public List<TModel> findByExample(TModel example, PagingInfo pagingInfo) {
        return this.findByExample(example, null, pagingInfo);
    }

    @Override
    public List<TModel> findByExample(TModel example, String orderBy, PagingInfo pagingInfo) {
        return this.findByExample(example, null, null, orderBy, pagingInfo);
    }

    @Override
    public List<TModel> findByExample(TModel example, String condition, Object[] paramValue) {
        return this.findByExample(example, condition, paramValue, null, null);
    }

    @Override
    public List<TModel> findByExample(TModel example, String condition, Object[] paramValue, String orderBy, PagingInfo pagingInfo) {
        return this.dao.findByExample(example, condition, paramValue, orderBy, pagingInfo);
    }

    @Override
    public List<TModel> query(String condition, Object[] paramValue) {
        return this.query(condition, paramValue, null);
    }

    @Override
    public List<TModel> query(String condition, Object[] paramValue, String orderBy) {
        return this.query(condition, paramValue,orderBy,null);
    }

    @Override
    public List<TModel> query(String condition, Object[] paramValue, String orderBy, PagingInfo pagingInfo) {
        return this.dao.findBySqlCondition(modelClass, condition, paramValue, orderBy, pagingInfo);
    }

    @Override
    public TModel save(TModel model) {
        return this.dao.save(model);
    }

    @Override
    public void remove(TModel model) {
        this.dao.remove(model);
    }

    @Override
    public void removeByPk(TKey id) {
        this.dao.removeByPk(modelClass, id);
    }

    @Override
    public Collection<TModel> saveAll(Collection<TModel> models) {
        return this.dao.saveAll(models);
    }

    @Override
    public void removeAll(Collection<TModel> models) {
        this.dao.removeAll(models);
    }

    @Override
    public void removeAllByPk(Collection<TKey> keys) {
        this.dao.removeAllByPk(modelClass, keys);
    }
}
