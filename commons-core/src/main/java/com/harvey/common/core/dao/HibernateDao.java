package com.harvey.common.core.dao;

import java.util.Collection;
import java.util.Date;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;

public interface HibernateDao {
    
    Criteria createCriteria(Class<?> modelClass);
    
    SQLQuery createSQLQuery(String sql);
    
    SessionFactory getSessionFactory();
    
    void setSessionFactory(SessionFactory sessionFactory);
    
    void flush();
    
    void evict(Object model);
    
    void evicts(Collection<?> models);
    
    Date getSysDate();
}
