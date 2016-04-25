package com.harvey.common.core.dao;

import java.util.List;
import java.util.Map;

import com.harvey.common.core.model.DynamicModelClass;
import com.harvey.common.core.model.PagingInfo;

public interface NativeSqlDao extends HibernateDao {
    
    void executeDDL(String sql);
    
    Integer[] batchUpdate(String sql,Object[][] parameters);
    
    int bulkUpdate(String sql, Object... parameters);
    
    int bulkUpdate(String sql, Map<String, Object> parameters);
    
    <TResult> TResult queryScalar(String sql, Object... args);

    <TResult> TResult queryScalar(String sql, Map<String, Object> args);
    
    <TResult> List<TResult> queryScalarList(String sql, Object... parameters);
    
    <TResult> List<TResult> queryScalarList(String sql, Map<String, Object> parameters);
    
    <TResult> List<TResult> queryList(Class<TResult> model,String sql, Object... parameters);
    
    <TResult> List<TResult> queryList(Class<TResult> model,String sql, Map<String, Object> parameters);
    
    List<DynamicModelClass> query(String sql, Map<String, Object> parameters);

    List<DynamicModelClass> query(String sql, Map<String, Object> parameters, PagingInfo pagingInfo);
    
    List<DynamicModelClass> query(String sql, Object... parameters);

    List<DynamicModelClass> query(String sql, Object[] parameters, PagingInfo pagingInfo);
    
    /**
     * 支持的格式为: sp_test(uid IN int,count OUT int)
     */    
    Map<String,Object> executeStoredProcedure(String spName,Map<String,Object> parameters);
}
