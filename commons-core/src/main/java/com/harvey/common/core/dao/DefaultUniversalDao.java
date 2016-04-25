package com.harvey.common.core.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.LockOptions;
import org.hibernate.SQLQuery;
import org.hibernate.StaleObjectStateException;
import org.hibernate.criterion.Projections;
import org.springframework.beans.BeanUtils;

import com.harvey.common.core.bean.DefaultBeanWrapper;
import com.harvey.common.core.context.Context;
import com.harvey.common.core.dao.utils.EntityUtils;
import com.harvey.common.core.dao.utils.QueryUtils;
import com.harvey.common.core.hibernate.DynamicModelClassResultTransformer;
import com.harvey.common.core.model.BaseModel;
import com.harvey.common.core.model.DynamicModelClass;
import com.harvey.common.core.model.ModelState;
import com.harvey.common.core.model.PagingInfo;
import com.harvey.common.core.model.UserBaseModel;
import com.harvey.common.core.reflectasm.MethodAccess;

public class DefaultUniversalDao extends AbstractHibernateDao implements UniversalDao {

	@Override
	public <MODEL> MODEL get(Class<MODEL> modelClass, Serializable id) {
		if (id == null) {
			return null;
		}
		Class<?> rootModelClass = EntityUtils.getEntityClass(modelClass);
		Object entity = this.sessionFactory.getCurrentSession().get(rootModelClass, id);
		if (entity != null) {
			evict(entity);
		}
		return EntityUtils.convertEntityType(entity, modelClass);
	}

	@Override
	public <MODEL> MODEL getForUpdate(Class<MODEL> modelClass, Serializable id) {
		if (id == null) {
			return null;
		}
		Class<?> rootModelClass = EntityUtils.getEntityClass(modelClass);
		Object entity = this.sessionFactory.getCurrentSession().get(rootModelClass, id, LockOptions.UPGRADE);
		if (entity != null) {
			evict(entity);
		}
		return EntityUtils.convertEntityType(entity, modelClass);
	}

	@Override
	public <MODEL> boolean exists(Class<MODEL> modelClass, Serializable id) {
		if (id == null) {
			return false;
		}
		Class<?> rootModelClass = EntityUtils.getEntityClass(modelClass);
		Object entity = this.sessionFactory.getCurrentSession().get(rootModelClass, id);
		return entity != null;
	}

	@Override
	public <MODEL> List<MODEL> getAll(Class<MODEL> modelClass) {
		return this.getAll(modelClass, null, null);
	}

	@Override
	public <MODEL> List<MODEL> getAll(Class<MODEL> modelClass, String orderBy) {
		return this.getAll(modelClass, orderBy, null);
	}

	@Override
	public <MODEL> List<MODEL> getAll(Class<MODEL> modelClass, String orderBy, PagingInfo pagingInfo) {
		Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(EntityUtils.getEntityClass(modelClass));
		if (pagingInfo != null) {
			criteria.setProjection(Projections.rowCount());
			Object ret = criteria.uniqueResult();
			if (ret != null) {
				pagingInfo.setTotalRows(((Long) ret).intValue());
			} else {
				pagingInfo.setTotalRows(0);
			}
			criteria.setProjection(null);
			QueryUtils.setPagingInfo(criteria, pagingInfo);
		}
		QueryUtils.addOrderBy(criteria, orderBy);
		List<?> queryResult = criteria.list();
		List<MODEL> result = new ArrayList<MODEL>();
		for (Object entity : queryResult) {
			evict(entity);
			result.add(EntityUtils.convertEntityType(entity, modelClass));
		}
		return result;
	}

	@Override
	public <MODEL> int getRowCount(Class<MODEL> modelClass) {
		Criteria criteria = this.createCriteria(EntityUtils.getEntityClass(modelClass));
		criteria.setProjection(Projections.rowCount());
		return ((Long) criteria.uniqueResult()).intValue();
	}

	@Override
	public <MODEL> List<MODEL> findBySqlCondition(Class<MODEL> modelClass, String sqlCondition, Object[] parameterValues) {
		return this.findBySqlCondition(modelClass, sqlCondition, parameterValues, null, null);
	}

	@Override
	public <MODEL> List<MODEL> findBySqlCondition(Class<MODEL> modelClass, String sqlCondition, Object[] parameterValues, String orderBy) {
		return this.findBySqlCondition(modelClass, sqlCondition, parameterValues, orderBy, null);
	}

	@Override
	public <MODEL> List<MODEL> findBySqlCondition(Class<MODEL> modelClass, String sqlCondition, Object[] parameterValues, String orderBy, PagingInfo pagingInfo) {
		Criteria criteria = this.createCriteria(EntityUtils.getEntityClass(modelClass));
		QueryUtils.addSqlCondtion(this.sessionFactory, criteria, sqlCondition, parameterValues);
		if (pagingInfo != null) {
			criteria.setProjection(Projections.rowCount());
			pagingInfo.setTotalRows(((Long) criteria.uniqueResult()).intValue());
			criteria.setProjection(null);
			QueryUtils.setPagingInfo(criteria, pagingInfo);
		}
		QueryUtils.addOrderBy(criteria, orderBy);
		List<?> queryResult = criteria.list();
		List<MODEL> result = new ArrayList<MODEL>();
		for (Object entity : queryResult) {
			evict(entity);
			result.add(EntityUtils.convertEntityType(entity, modelClass));
		}
		return result;
	}

	@Override
	public <MODEL> int getRowCountBySqlCondition(Class<MODEL> modelClass, String sqlCondition, Object[] parameterValues) {
		Criteria criteria = this.createCriteria(EntityUtils.getEntityClass(modelClass));
		QueryUtils.addSqlCondtion(this.sessionFactory, criteria, sqlCondition, parameterValues);
		criteria.setProjection(Projections.rowCount());
		return ((Long) criteria.uniqueResult()).intValue();
	}

	@Override
	public <MODEL> List<MODEL> findByExample(MODEL example) {
		return this.findByExample(example, null, null);
	}

	@Override
	public <MODEL> List<MODEL> findByExample(MODEL example, String orderBy) {
		return this.findByExample(example, orderBy, null);
	}

	@Override
	public <MODEL> List<MODEL> findByExample(MODEL example, String orderBy, PagingInfo pagingInfo) {
		return this.findByExample(example, null, null, orderBy, pagingInfo);
	}

	@Override
	public <MODEL> List<MODEL> findByExample(MODEL example, String sqlCondition, Object[] parameterValues, String orderBy, PagingInfo pagingInfo) {
		String entityName = EntityUtils.getEntityClass(example.getClass()).getName();
		Criteria criteria = entityName != null ? this.sessionFactory.getCurrentSession().createCriteria(entityName) : this.sessionFactory.getCurrentSession()
		        .createCriteria(example.getClass());
		QueryUtils.addExample(criteria, example);
		QueryUtils.addSqlCondtion(this.sessionFactory, criteria, sqlCondition, parameterValues);
		if (pagingInfo != null) {
			criteria.setProjection(Projections.rowCount());
			pagingInfo.setTotalRows(((Long) criteria.uniqueResult()).intValue());
			criteria.setProjection(null);
			QueryUtils.setPagingInfo(criteria, pagingInfo);
		}
		QueryUtils.addOrderBy(criteria, orderBy);
		List<?> queryResult = criteria.list();
		List<MODEL> result = new ArrayList<MODEL>();
		for (Object entity : queryResult) {
			evict(entity);
			result.add(EntityUtils.convertEntityType(entity, (Class<MODEL>) example.getClass()));
		}
		return result;
	}

	@Override
	public <MODEL> int getRowCountByExample(MODEL example) {
		return this.getRowCountByExample(example, null, null);
	}

	@Override
	public <MODEL> int getRowCountByExample(MODEL example, String sqlCondition, Object[] parameterValues) {
		String entityName = EntityUtils.getEntityClass(example.getClass()).getName();
		Criteria criteria = entityName != null ? this.sessionFactory.getCurrentSession().createCriteria(entityName) : this.sessionFactory.getCurrentSession()
		        .createCriteria(example.getClass());
		QueryUtils.addExample(criteria, example);
		QueryUtils.addSqlCondtion(this.sessionFactory, criteria, sqlCondition, parameterValues);
		criteria.setProjection(Projections.rowCount());
		return ((Long) criteria.uniqueResult()).intValue();
	}

	private <MODEL> MODEL saveNormalModel(MODEL model) {
		/*if (model instanceof CreatingInfo || model instanceof ModifyingInfo) {
			Date sysDate = getSysDate();
			UserBaseModel user = Context.getCurrentUser();
			Serializable userId = user != null ? user.primeryKeyValue() : null;
			if (model instanceof CreatingInfo) {
				CreatingInfo creatingInfo = (CreatingInfo) model;
				if (creatingInfo.getCreateTime() == null && creatingInfo.getCreator() == null) {
					creatingInfo.setCreator(userId + "");
					creatingInfo.setCreateTime(sysDate);
				}
			}
			if (model instanceof ModifyingInfo) {
				ModifyingInfo modifyingInfo = (ModifyingInfo) model;
				modifyingInfo.setModifier(userId + "");
				modifyingInfo.setModifyTime(sysDate);
			}
		}*/
	    Class<?> entityClass = EntityUtils.getEntityClass(model.getClass());
	    String entityName = entityClass.getName();
	    setOperatorInfo(model,entityClass);
		Object entity = this.sessionFactory.getCurrentSession().save(entityName, model);
		if (model.getClass() == entity.getClass()) {
			return (MODEL) entity;
		} else {
			BeanUtils.copyProperties(entity, model);
			return model;
		}
	}
	
	private void setOperatorInfo(Object model,Class<?> entityClass){
        UserBaseModel user = Context.getCurrentUser();
        Serializable userId = user != null ? user.primeryKeyValue() : null;
        MethodAccess access = MethodAccess.get(entityClass);
        Date date = null;
        if(access.hasMethod("getCreateTime") && access.tryInvoke(model, "getCreateTime") == null){
            date = getSysDate();
            access.tryInvoke(model, "setCreateTime",date);
            access.tryInvoke(model, "setCreator",userId);
        }
        if(access.hasMethod("setModifyTime")){
            if(date == null) date = getSysDate();
            access.tryInvoke(model, "setModifyTime",date);
            access.tryInvoke(model, "setModifier",userId);            
        }
	}

	@Override
	public <MODEL> MODEL save(MODEL model) {
		try {
			BaseModel baseModel = null;
			if (model instanceof BaseModel) {
				baseModel = (BaseModel) model;
			} else {
				return saveNormalModel(model);
			}
			if (ModelState.Deleted.equals(baseModel.getModelState())) {
				this.remove(model);
				return null;
			} else {
				Serializable id = null;
				if (baseModel.validFields().size() != 0 && baseModel.validFields().size() != EntityUtils.getFieldCount(model.getClass())) {
					id = baseModel.primeryKeyValue();
					if (id != null) {
						Object persistantModel = this.sessionFactory.getCurrentSession().get(EntityUtils.getEntityClass(model.getClass()), id);
						if (persistantModel != null) {
							evict(persistantModel);
							new DefaultBeanWrapper(baseModel).copyPropertiesTo(persistantModel, Collections.unmodifiableList(baseModel.validFields()));
							if (model.getClass() == persistantModel.getClass()) {
								model = (MODEL) persistantModel;
							} else {
								BeanUtils.copyProperties(persistantModel, model);
							}
						}
					}
				}
				/*if (model instanceof CreatingInfo || model instanceof ModifyingInfo) {
					Date sysDate = getSysDate();
					UserBaseModel user = Context.getCurrentUser();
					Serializable userId = user != null ? user.primeryKeyValue() : null;
					if (model instanceof CreatingInfo) {
						CreatingInfo creatingInfo = (CreatingInfo) model;
						if (creatingInfo.getCreateTime() == null && creatingInfo.getCreator() == null) {
							creatingInfo.setCreator(userId + "");
							creatingInfo.setCreateTime(sysDate);
						}
					}
					if (model instanceof ModifyingInfo) {
						ModifyingInfo modifyingInfo = (ModifyingInfo) model;
						modifyingInfo.setModifier(userId + "");
						modifyingInfo.setModifyTime(sysDate);
					}
				}*/
				Class<?> entityClass = EntityUtils.getEntityClass(model.getClass());
				setOperatorInfo(model,entityClass);
				Object entity = this.sessionFactory.getCurrentSession().merge(entityClass.getName(), model);
				flush();
				evict(entity);
				if (model.getClass() == entity.getClass()) {
					if (id == null) {
						id = EntityUtils.getId(entity);
						EntityUtils.setId(model, id);
					}
					return (MODEL) entity;
				} else {
					BeanUtils.copyProperties(entity, model);
					return model;
				}
			}
		} catch (StaleObjectStateException sosex) {
			throw new RuntimeException("Data has been modified by another user. Please reload and try again.");
		}
	}

	@Override
	public <MODEL> Collection<MODEL> saveAll(Collection<MODEL> models) {
		try {
			if (models.isEmpty()) {
				return models;
			}
			List<MODEL> modelsToDelete = new ArrayList<MODEL>();
			List<MODEL> modelsToMerge = new ArrayList<MODEL>();
			for (MODEL model : models) {
				if (!(model instanceof BaseModel)) {
					continue;
				}

				BaseModel baseModel = (BaseModel) model;
				if (ModelState.Deleted.equals(baseModel.getModelState())) {
					modelsToDelete.add(model);
				} else {
					if (baseModel.validFields().size() != 0 && baseModel.validFields().size() != EntityUtils.getFieldCount(model.getClass())) {
						Serializable id = baseModel.primeryKeyValue();
						if (id != null) {
							Object persistantModel = this.sessionFactory.getCurrentSession().get(EntityUtils.getEntityClass(model.getClass()), id);
							if (persistantModel != null) {
								evict(persistantModel);
								new DefaultBeanWrapper(model).copyPropertiesTo(persistantModel, Collections.unmodifiableList(baseModel.validFields()));
								if (model.getClass() == persistantModel.getClass()) {
									model = (MODEL) persistantModel;
								} else {
									BeanUtils.copyProperties(persistantModel, model);
								}
							}
						}
					}
					modelsToMerge.add(model);
				}
			}
			this.removeAll(modelsToDelete);
			flush();
			if (modelsToMerge.size() == 0) {
				return Collections.emptyList();
			} else {
			    Class<?> entityClass = EntityUtils.getEntityClass(modelsToMerge.iterator().next().getClass());
				String entityName = entityClass.getName();
				List<Object> mergeResult = new ArrayList<Object>();
				for (MODEL model : modelsToMerge) {
					setOperatorInfo(model,entityClass);
					mergeResult.add(this.sessionFactory.getCurrentSession().merge(entityName, model));
				}
				flush();
				List<MODEL> result = new ArrayList<MODEL>();
				for (int i = 0; i < modelsToMerge.size(); i++) {
					MODEL model = modelsToMerge.get(i);
					Object entity = mergeResult.get(i);
					evict(entity);
					if (model.getClass() == entity.getClass()) {
						Serializable id = EntityUtils.getId(entity);
						EntityUtils.setId(model, id);						
						result.add((MODEL) entity);
					} else {
						BeanUtils.copyProperties(entity, model);
						result.add(model);
					}
				}
				return result;
			}
		} catch (StaleObjectStateException sosex) {
			throw new RuntimeException("Data has been modified by another user. Please reload and try again.");
		}
	}

	@Override
	public <MODEL> void remove(MODEL model) {
		try {
			this.sessionFactory.getCurrentSession().delete(EntityUtils.getEntityClass(model.getClass()).getName(), model);
			flush();
		} catch (StaleObjectStateException sosex) {
			throw new RuntimeException("Data has been modified by another user. Please reload and try again.");
		}
	}

	@Override
	public <MODEL> void removeAll(Collection<MODEL> models) {
		try {
			if (models.size() == 0) {
				return;
			} else {
				String entityName = EntityUtils.getEntityClass(models.iterator().next().getClass()).getName();
				for (MODEL model : models) {
					this.sessionFactory.getCurrentSession().delete(entityName, model);
				}
				flush();
			}
		} catch (StaleObjectStateException sosex) {
			throw new RuntimeException("Data has been modified by another user. Please reload and try again.");
		}
	}

	@Override
	public <MODEL> void removeByPk(Class<MODEL> modelClass, Serializable id) {
		this.remove(this.get(modelClass, id));
	}

	@Override
	public <MODEL> void removeAllByPk(Class<MODEL> modelClass, Collection<? extends Serializable> ids) {
		List<MODEL> models = new ArrayList<MODEL>();
		for (Serializable id : ids) {
			models.add(this.get(modelClass, id));
		}
		this.removeAll(models);
	}

	@Override
	public <ITEM> List<ITEM> query(Object condition, Class<ITEM> itemClass) {
		return this.query(condition, itemClass, null, null, null, null);
	}

	@Override
	public <ITEM> List<ITEM> query(Object condition, Class<ITEM> itemClass, String orderBy) {
		return this.query(condition, itemClass, orderBy, null);
	}

	@Override
	public <ITEM> List<ITEM> query(Object condition, Class<ITEM> itemClass, PagingInfo pagingInfo) {
		return this.query(condition, itemClass, null, null, null, pagingInfo);
	}

	@Override
	public <ITEM> List<ITEM> query(Object condition, Class<ITEM> itemClass, String orderBy, PagingInfo pagingInfo) {
		return this.query(condition, itemClass, null, null, orderBy, pagingInfo);
	}

	@Override
	public <ITEM> List<ITEM> query(Object condition, Class<ITEM> itemClass, String extraSqlCondition, Object[] parameterValues, String orderBy,
	        PagingInfo pagingInfo) {
		Class<?> resultEntityClass;
		if (!BaseModel.class.isAssignableFrom(itemClass) || BaseModel.class.equals(itemClass)) {
			resultEntityClass = QueryUtils.getQueryItemClass(condition);
		} else {
			resultEntityClass = EntityUtils.getEntityClass(itemClass);
		}
		Map<String,Object> conditionMap = new HashMap<String,Object>(QueryUtils.toNamedParameters(condition));
		String sql = QueryUtils.getDynamicSql(this.sessionFactory, QueryUtils.getSqlQueryName(condition), conditionMap);

		String sqlCondition = QueryUtils.toNamedParameters(this.sessionFactory, extraSqlCondition);
		String runSql = QueryUtils.addExtraConditions(sql, sqlCondition);

		runSql = "select T__UUID__.*, " + this.getSelectGUIDString() + " as UUID__ from (" + runSql + ") T__UUID__";

		runSql = QueryUtils.addOrderBy(runSql, orderBy);

		SQLQuery queryObject = this.sessionFactory.getCurrentSession().createSQLQuery(runSql);
		
        if (parameterValues != null) {
            conditionMap.putAll(QueryUtils.toNamedParameters(parameterValues));
        }		
		queryObject.setProperties(conditionMap);
		queryObject.addEntity(resultEntityClass);
		if (pagingInfo != null) {
			pagingInfo.setTotalRows(this.queryRowCount(condition, extraSqlCondition, parameterValues));
			QueryUtils.setPagingInfo(queryObject, pagingInfo);
		}
		List<Object> queryResult = queryObject.list();
		List<ITEM> result = new ArrayList<ITEM>();
		for (Object entity : queryResult) {
			evict(entity);
			result.add(EntityUtils.convertEntityType(entity, itemClass));
		}
		return result;
	}
	
    @Override
    public <ITEM> List<ITEM> query(String queryName, Class<ITEM> itemClass,Map<String, Object> parameters, String extraSqlCondition, Object[] parameterValues, String orderBy, PagingInfo pagingInfo) {
        itemClass = (Class<ITEM>) EntityUtils.getEntityClass(itemClass);
        parameters = parameters != null ? new HashMap<String,Object>(parameters) : new HashMap<String,Object>();
        
        String sql = QueryUtils.getDynamicSql(this.sessionFactory, queryName, parameters);
        String sqlCondition = QueryUtils.toNamedParameters(this.sessionFactory, extraSqlCondition);
        String runSql = QueryUtils.addExtraConditions(sql, sqlCondition);

        runSql = "select T__UUID__.*, " + this.getSelectGUIDString() + " as UUID__ from (" + runSql + ") T__UUID__";

        runSql = QueryUtils.addOrderBy(runSql, orderBy);

        SQLQuery queryObject = this.sessionFactory.getCurrentSession().createSQLQuery(runSql);
        
        if (parameterValues != null) {
            parameters.putAll(QueryUtils.toNamedParameters(parameterValues));
        }       
        queryObject.setProperties(parameters);
        queryObject.addEntity(itemClass);
        if (pagingInfo != null) {
            pagingInfo.setTotalRows(this.queryRowCountBySql(sql, parameters, extraSqlCondition, parameterValues));
            QueryUtils.setPagingInfo(queryObject, pagingInfo);
        }
        List<Object> queryResult = queryObject.list();
        List<ITEM> result = new ArrayList<ITEM>();
        for (Object entity : queryResult) {
            evict(entity);
            result.add(EntityUtils.convertEntityType(entity, itemClass));
        }
        return result;
    }	

	@Override
	public int queryRowCount(Object condition, String extraSqlCondition, Object[] parameterValues) {
	    Map<String, Object> parameters = QueryUtils.toNamedParameters(condition);
	    String sql = QueryUtils.getDynamicSql(this.sessionFactory, QueryUtils.getSqlQueryName(condition), parameters);
		return super.queryRowCountBySql(sql, parameters, extraSqlCondition, parameterValues);
	}

	@Override
	public List<DynamicModelClass> query(String queryName, Map<String, Object> parameters) {
		return this.query(queryName, parameters, null, null);
	}

	@Override
	public List<DynamicModelClass> query(String queryName, Map<String, Object> parameters, String orderBy) {
		return this.query(queryName, parameters, orderBy, null);
	}

	@Override
	public List<DynamicModelClass> query(String queryName, Map<String, Object> parameters, PagingInfo pagingInfo) {
		return this.query(queryName, parameters, null, pagingInfo);
	}

	@Override
	public List<DynamicModelClass> query(String queryName, Map<String, Object> parameters, String orderBy, PagingInfo pagingInfo) {
		return this.query(queryName, parameters, null, null, orderBy, pagingInfo);
	}

	@Override
	public List<DynamicModelClass> query(String queryName, Map<String, Object> parameters, String extraSqlCondition, Object[] parameterValues, String orderBy,
	        PagingInfo pagingInfo) {
		String sql = QueryUtils.getDynamicSql(this.sessionFactory, queryName, parameters);
		String sqlCondition = QueryUtils.toNamedParameters(this.sessionFactory, extraSqlCondition);
		String runSql = QueryUtils.addExtraConditions(sql, sqlCondition);

		runSql = "select T__UUID__.*, " + this.getSelectGUIDString() + " as UUID__ from (" + runSql + ") T__UUID__";

		runSql = QueryUtils.addOrderBy(runSql, orderBy);
		parameters = parameters != null ? new HashMap<String,Object>(parameters) : new HashMap<String,Object>();
		SQLQuery queryObject = this.sessionFactory.getCurrentSession().createSQLQuery(runSql);
		if (parameterValues != null) {
		    parameters.putAll(QueryUtils.toNamedParameters(parameterValues));
		}
		queryObject.setProperties(parameters);
		queryObject.setResultTransformer(DynamicModelClassResultTransformer.getInstance());
		if (pagingInfo != null) {
			pagingInfo.setTotalRows(this.queryRowCountBySql(sql, parameters, extraSqlCondition, parameterValues));
			QueryUtils.setPagingInfo(queryObject, pagingInfo);
		}
		List<DynamicModelClass> queryResult = queryObject.list();

		return queryResult;
	}

	@Override
	public int queryRowCount(String queryName, Map<String, Object> parameters, String extraSqlCondition, Object[] parameterValues) {
		String sql = QueryUtils.getDynamicSql(this.sessionFactory, queryName, parameters);
		return super.queryRowCountBySql(sql, parameters, extraSqlCondition, parameterValues);
	}

	@Override
	public int update(Object condition) {
		String sql = QueryUtils.getNamedSql(this.sessionFactory, QueryUtils.getSqlUpdateName(condition));
		SQLQuery queryObject = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
		queryObject.setProperties(condition);
		return queryObject.executeUpdate();
	}

	@Override
	public int update(String updateName, Map<String, Object> parameters) {
		String sql = QueryUtils.getNamedSql(this.sessionFactory, updateName);
		SQLQuery queryObject = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
		queryObject.setProperties(parameters);
		return queryObject.executeUpdate();
	}

}
