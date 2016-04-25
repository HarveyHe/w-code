package com.harvey.common.core.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.harvey.common.core.model.DynamicModelClass;
import com.harvey.common.core.model.PagingInfo;

public interface UniversalDao extends HibernateDao {

	<MODEL> MODEL get(Class<MODEL> modelClass, Serializable id);

	<MODEL> MODEL getForUpdate(Class<MODEL> modelClass, Serializable id);

	<MODEL> boolean exists(Class<MODEL> modelClass, Serializable id);

	<MODEL> List<MODEL> getAll(Class<MODEL> modelClass);

	<MODEL> List<MODEL> getAll(Class<MODEL> modelClass, String orderBy);

	<MODEL> List<MODEL> getAll(Class<MODEL> modelClass, String orderBy, PagingInfo pagingInfo);

	<MODEL> int getRowCount(Class<MODEL> modelClass);

	<MODEL> List<MODEL> findBySqlCondition(Class<MODEL> modelClass, String sqlCondition, Object[] parameterValues);

	<MODEL> List<MODEL> findBySqlCondition(Class<MODEL> modelClass, String sqlCondition, Object[] parameterValues, String orderBy);

	<MODEL> List<MODEL> findBySqlCondition(Class<MODEL> modelClass, String sqlCondition, Object[] parameterValues, String orderBy, PagingInfo pagingInfo);

	<MODEL> int getRowCountBySqlCondition(Class<MODEL> modelClass, String sqlCondition, Object[] parameterValues);

	<MODEL> List<MODEL> findByExample(MODEL example);

	<MODEL> List<MODEL> findByExample(MODEL example, String orderBy);

	<MODEL> List<MODEL> findByExample(MODEL example, String orderBy, PagingInfo pagingInfo);

	<MODEL> List<MODEL> findByExample(MODEL example, String sqlCondition, Object[] parameterValues, String orderBy, PagingInfo pagingInfo);

	<MODEL> int getRowCountByExample(MODEL example);

	<MODEL> int getRowCountByExample(MODEL example, String sqlCondition, Object[] parameterValues);

	<MODEL> MODEL save(MODEL model);

	<MODEL> Collection<MODEL> saveAll(Collection<MODEL> models);

	<MODEL> void remove(MODEL model);

	<MODEL> void removeAll(Collection<MODEL> models);

	<MODEL> void removeByPk(Class<MODEL> modelClass, Serializable id);

	<MODEL> void removeAllByPk(Class<MODEL> modelClass, Collection<? extends Serializable> ids);

	<ITEM> List<ITEM> query(Object condition, Class<ITEM> itemClass);

	<ITEM> List<ITEM> query(Object condition, Class<ITEM> itemClass, String orderBy);

	<ITEM> List<ITEM> query(Object condition, Class<ITEM> itemClass, PagingInfo pagingInfo);

	<ITEM> List<ITEM> query(Object condition, Class<ITEM> itemClass, String orderBy, PagingInfo pagingInfo);

	<ITEM> List<ITEM> query(Object condition, Class<ITEM> itemClass, String extraSqlCondition, Object[] parameterValues, String orderBy, PagingInfo pagingInfo);

	<ITEM> List<ITEM> query(String queryName, Class<ITEM> itemClass,Map<String, Object> parameters, String extraSqlCondition, Object[] parameterValues, String orderBy, PagingInfo pagingInfo);
	
	int queryRowCount(Object condition, String extraSqlCondition, Object[] parameterValues);

	List<DynamicModelClass> query(String queryName, Map<String, Object> parameters);

	List<DynamicModelClass> query(String queryName, Map<String, Object> parameters, String orderBy);

	List<DynamicModelClass> query(String queryName, Map<String, Object> parameters, PagingInfo pagingInfo);

	List<DynamicModelClass> query(String queryName, Map<String, Object> parameters, String orderBy, PagingInfo pagingInfo);

	List<DynamicModelClass> query(String queryName, Map<String, Object> parameters, String extraSqlCondition, Object[] parameterValues, String orderBy,
	        PagingInfo pagingInfo);

	int queryRowCount(String queryName, Map<String, Object> parameters, String extraSqlCondition, Object[] parameterValues);

	int update(Object condition);

	int update(String updateName, Map<String, Object> parameters);
}
