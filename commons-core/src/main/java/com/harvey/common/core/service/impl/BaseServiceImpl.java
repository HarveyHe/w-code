package com.harvey.common.core.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.harvey.common.core.dao.NativeSqlDao;
import com.harvey.common.core.dao.UniversalDao;
import com.harvey.common.core.service.BaseService;

public abstract class BaseServiceImpl implements BaseService {
    
    protected Log log = LogFactory.getLog(this.getClass());

    @Autowired
    protected UniversalDao dao;

    @Autowired
    protected NativeSqlDao sqlDao;
    
    protected void setRollbackOnly() {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }
}
