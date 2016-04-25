package com.harvey.common.core.repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.harvey.common.core.model.BaseModel;
import com.harvey.common.core.model.PagingInfo;

public interface BaseRepository<TModel extends BaseModel,TKey extends Serializable> {
    
    TModel get(TKey id);
    
    List<TModel> getAll();
    
    List<TModel> getAll(String orderBy);
    
    List<TModel> getAll(PagingInfo pagingInfo);
    
    List<TModel> getAll(String orderBy,PagingInfo pagingInfo);
    
    List<TModel> findByExample(TModel example);
    
    List<TModel> findByExample(TModel example,PagingInfo pagingInfo);
    
    List<TModel> findByExample(TModel example,String orderBy,PagingInfo pagingInfo);
    
    List<TModel> findByExample(TModel example,String condition,Object[] paramValue);
    
    List<TModel> findByExample(TModel example,String condition,Object[] paramValue,String orderBy,PagingInfo pagingInfo);
    
    List<TModel> query(String condition,Object[] paramValue);
    
    List<TModel> query(String condition,Object[] paramValue,String orderBy);
    
    List<TModel> query(String condition,Object[] paramValue,String orderBy,PagingInfo pagingInfo);
    
    TModel save(TModel model);
    
    Collection<TModel> saveAll(Collection<TModel> models);
    
    void remove(TModel model);
    
    void removeByPk(TKey id);
    
    void removeAll(Collection<TModel> models);
    
    void removeAllByPk(Collection<TKey> keys);
}
