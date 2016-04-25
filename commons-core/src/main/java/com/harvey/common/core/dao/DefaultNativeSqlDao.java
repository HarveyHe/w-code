package com.harvey.common.core.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.SQLQuery;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;

import com.harvey.common.core.bean.StoredProcedureBean;
import com.harvey.common.core.dao.utils.EntityUtils;
import com.harvey.common.core.dao.utils.QueryUtils;
import com.harvey.common.core.hibernate.DynamicModelClassResultTransformer;
import com.harvey.common.core.model.DynamicModelClass;
import com.harvey.common.core.model.PagingInfo;

public class DefaultNativeSqlDao extends AbstractHibernateDao implements NativeSqlDao {

    @Override
    public void executeDDL(final String sql) {
        this.sessionFactory.getCurrentSession().doWork(new Work() {
            public void execute(Connection conn) throws SQLException {
                Statement stmt = null;
                try {
                    stmt = conn.createStatement();
                    stmt.execute(sql);
                } finally {
                    try {
                        stmt.close();
                    } catch (Exception ex) {
                    }
                }
            }
        });
    }

    @Override
    public Integer[] batchUpdate(final String sql, final Object[][] parameters) {
        return this.sessionFactory.getCurrentSession().doReturningWork(new ReturningWork<Integer[]>() {

            @Override
            public Integer[] execute(Connection connection) throws SQLException {
                PreparedStatement ps = null;
                try {
                    ps = connection.prepareStatement(sql);
                    for (int i = 0; i < parameters.length; i++) {
                        for (int j = 0; j < parameters[i].length; j++) {
                            ps.setObject(j+1, parameters[i][j]);
                        }
                        ps.addBatch();
                    }
                    return ArrayUtils.toObject(ps.executeBatch());
                } finally {
                    if (ps != null) {
                        ps.close();
                    }
                }
            }

        });
    }

    @Override
    public int bulkUpdate(String sql, Object... parameters) {
        SQLQuery queryObject = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
        setParameter(queryObject, parameters);
        return queryObject.executeUpdate();
    }

    @Override
    public int bulkUpdate(String sql, Map<String, Object> parameters) {
        SQLQuery queryObject = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
        setParameter(queryObject, parameters);
        return queryObject.executeUpdate();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TResult> TResult queryScalar(String sql, Object... args) {
        SQLQuery query = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
        setParameter(query, args);
        return (TResult) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TResult> TResult queryScalar(String sql, Map<String, Object> args) {
        SQLQuery query = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
        setParameter(query, args);
        return (TResult) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TResult> List<TResult> queryScalarList(String sql, Object... parameters) {
        SQLQuery queryObject = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
        setParameter(queryObject, parameters);
        List<?> dataList = queryObject.list();
        List<TResult> result = new ArrayList<TResult>(dataList.size());
        for (Object dataItem : dataList) {
            if (dataItem instanceof Object[]) {
                result.add((TResult) ((Object[]) dataItem)[0]);
            } else {
                result.add((TResult) dataItem);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TResult> List<TResult> queryScalarList(String sql, Map<String, Object> parameters) {
        SQLQuery queryObject = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
        setParameter(queryObject, parameters);
        List<?> dataList = queryObject.list();
        List<TResult> result = new ArrayList<TResult>(dataList.size());
        for (Object dataItem : dataList) {
            if (dataItem instanceof Object[]) {
                result.add((TResult) ((Object[]) dataItem)[0]);
            } else {
                result.add((TResult) dataItem);
            }
        }
        return result;
    }

    @Override
    public List<DynamicModelClass> query(String sql, Map<String, Object> parameters) {
        return this.query(sql, parameters, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DynamicModelClass> query(String sql, Map<String, Object> parameters, PagingInfo pagingInfo) {
        SQLQuery query = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
        setParameter(query, parameters);
        if (pagingInfo != null) {
            Integer totalRow = super.queryRowCountBySql(sql, parameters, null, null);
            pagingInfo.setTotalRows(totalRow);
            QueryUtils.setPagingInfo(query, pagingInfo);
        }
        query.setResultTransformer(DynamicModelClassResultTransformer.getInstance());
        return query.list();
    }

    @Override
    public List<DynamicModelClass> query(String sql, Object... parameters) {
        return this.query(sql, parameters, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DynamicModelClass> query(String sql, Object[] parameters, PagingInfo pagingInfo) {
        SQLQuery query = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
        setParameter(query, parameters);
        if (pagingInfo != null) {
            Integer totalRow = super.queryRowCountBySql(sql, parameters, null, null);
            pagingInfo.setTotalRows(totalRow);
            QueryUtils.setPagingInfo(query, pagingInfo);
        }
        query.setResultTransformer(DynamicModelClassResultTransformer.getInstance());
        return query.list();
    }

    @Override
    public <TResult> List<TResult> queryList(Class<TResult> model, String sql, Object... parameters) {
        Class<?> resultEntityClass = EntityUtils.getEntityClass(model);
        SQLQuery query = this.sessionFactory.getCurrentSession().createSQLQuery(sql).addEntity(resultEntityClass);
        setParameter(query, parameters);
        List<?> resultList = query.list();
        List<TResult> results = new ArrayList<TResult>();
        for (Object item : resultList) {
            evict(item);
            results.add(EntityUtils.convertEntityType(item, model));
        }
        return results;
    }

    @Override
    public <TResult> List<TResult> queryList(Class<TResult> model, String sql, Map<String, Object> parameters) {
        Class<?> resultEntityClass = EntityUtils.getEntityClass(model);
        SQLQuery query = this.sessionFactory.getCurrentSession().createSQLQuery(sql).addEntity(resultEntityClass);
        setParameter(query, parameters);
        List<?> resultList = query.list();
        List<TResult> results = new ArrayList<TResult>();
        for (Object item : resultList) {
            evict(item);
            results.add(EntityUtils.convertEntityType(item, model));
        }
        return results;
    }

    @Override
    public Map<String, Object> executeStoredProcedure(String spName, Map<String, Object> parameters) {
        try {
            StoredProcedureBean spBean = StoredProcedureBean.parseProcedure(spName, parameters, this.sessionFactory);
            return spBean.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
