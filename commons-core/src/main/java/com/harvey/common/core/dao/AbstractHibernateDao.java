package com.harvey.common.core.dao;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.StandardBasicTypes;

import com.harvey.common.core.dao.utils.QueryUtils;

public abstract class AbstractHibernateDao implements HibernateDao {

    protected SessionFactory sessionFactory;
    private Date dbDate;
    private Long timeForLastDbDate = Long.valueOf(0);
    private String selectGUIDString;

    @Override
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public Criteria createCriteria(Class<?> modelClass) {
        return this.sessionFactory.getCurrentSession().createCriteria(modelClass);
    }

    @Override
    public SQLQuery createSQLQuery(String sql) {
        return sessionFactory.getCurrentSession().createSQLQuery(sql);
    }

    @Override
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void flush() {
        sessionFactory.getCurrentSession().flush();
    }

    @Override
    public void evict(Object model) {
        sessionFactory.getCurrentSession().evict(model);
    }
    
    @Override
    public void evicts(Collection<?> models){
        for(Object model:models){
            this.evict(model);
        }
    }

    @Override
    public Date getSysDate() {
        if (System.currentTimeMillis() - this.timeForLastDbDate > 1000) {
            synchronized (this.timeForLastDbDate) {
                if (System.currentTimeMillis() - this.timeForLastDbDate > 1000) {
                    Dialect dialect = ((SessionFactoryImplementor) this.sessionFactory).getDialect();
                    String sql = dialect.getCurrentTimestampSelectString();
                    int fromIndex = sql.indexOf(" from ");
                    if (fromIndex == -1) {
                        sql = sql + " as SYSDATE__";
                    } else {
                        sql = sql.substring(0, fromIndex) + " as SYSDATE__" + sql.substring(fromIndex);
                    }
                    SQLQuery queryObject = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
                    queryObject.addScalar("SYSDATE__", StandardBasicTypes.TIMESTAMP);
                    this.dbDate = new Date(((Timestamp) queryObject.uniqueResult()).getTime());
                    this.timeForLastDbDate = System.currentTimeMillis();
                }
            }
        }
        return this.dbDate;
    }

    protected void setParameter(Query query, Object... args) {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                query.setParameter(i, args[i]);
            }
        }
    }

    protected void setParameter(Query query, Map<String, Object> parameters) {
        if (parameters != null) {
            for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
                query.setParameter(parameter.getKey(), parameter.getValue());
            }
        }
    }

    protected int queryRowCountBySql(String sql, Map<String, Object> parameters, String extraSqlCondition, Object[] parameterValues) {
        String sqlCondition = QueryUtils.toNamedParameters(this.sessionFactory, extraSqlCondition);
        String runSql = QueryUtils.addExtraConditions(sql, sqlCondition);
        parameters = parameters != null ? new HashMap<String,Object>(parameters) : new HashMap<String,Object>();
        runSql = "select count(*) as COUNT__ from (" + runSql + ") T__COUNT__";
        SQLQuery queryObject = this.sessionFactory.getCurrentSession().createSQLQuery(runSql);
        if (parameterValues != null) {
            parameters.putAll(QueryUtils.toNamedParameters(parameterValues));
        }
        queryObject.setProperties(parameters);
        queryObject.addScalar("COUNT__", StandardBasicTypes.INTEGER);
        return (Integer) queryObject.uniqueResult();
    }
    
    protected int queryRowCountBySql(String sql, Object[] parameters, String extraSqlCondition, Object[] parameterValues) {
        String sqlCondition = QueryUtils.toNamedParameters(this.sessionFactory, extraSqlCondition);
        String runSql = QueryUtils.addExtraConditions(sql, sqlCondition);

        runSql = "select count(*) as COUNT__ from (" + runSql + ") T__COUNT__";
        SQLQuery queryObject = this.sessionFactory.getCurrentSession().createSQLQuery(runSql);
        this.setParameter(queryObject, parameters);
        if (parameterValues != null) {
            queryObject.setProperties(QueryUtils.toNamedParameters(parameterValues));
        }
        queryObject.addScalar("COUNT__", StandardBasicTypes.INTEGER);
        return (Integer) queryObject.uniqueResult();
    }
    
    protected String getSelectGUIDString(){
        if(this.selectGUIDString == null){
            Dialect dialect = ((SessionFactoryImplementor) this.sessionFactory).getDialect(); 
            String guidStr = dialect.getSelectGUIDString(); 
            guidStr = guidStr.replace("select ", "").replace(" from dual", ""); 
            this.selectGUIDString = guidStr;
        }
        return this.selectGUIDString;
    }
}
