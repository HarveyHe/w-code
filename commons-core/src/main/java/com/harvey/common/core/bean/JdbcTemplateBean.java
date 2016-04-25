package com.harvey.common.core.bean;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.jdbc.ReturningWork;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;

public class JdbcTemplateBean extends JdbcTemplate {

    private SessionFactory sessionFactory;

    public JdbcTemplateBean() {
        super();
    }

    public JdbcTemplateBean(DataSource dataSource, boolean lazyInit) {
        super(dataSource, lazyInit);
    }

    public JdbcTemplateBean(DataSource dataSource) {
        super(dataSource);
    }

    public JdbcTemplateBean(SessionFactory sessionFactory) {
        this();
        this.sessionFactory = sessionFactory;
    }

    @Override
    public <T> T execute(final CallableStatementCreator csc, final CallableStatementCallback<T> action) throws DataAccessException {
        if (sessionFactory == null) {
            return super.execute(csc, action);
        }
        Assert.notNull(csc, "CallableStatementCreator must not be null");
        Assert.notNull(action, "Callback object must not be null");
        if (this.logger.isDebugEnabled() && csc instanceof SqlProvider) {
            String sql = ((SqlProvider) csc).getSql();
            this.logger.debug("Calling stored procedure" + ((sql != null) ? " [" + sql + "]" : ""));
        }
        return sessionFactory.getCurrentSession().doReturningWork(new ReturningWork<T>() {

            @Override
            public T execute(Connection connection) throws SQLException {
                CallableStatement cs = null;
                try {
                    Connection conToUse = connection;
                    cs = csc.createCallableStatement(conToUse);
                    applyStatementSettings(cs);
                    CallableStatement csToUse = cs;
                    Object result = action.doInCallableStatement(csToUse);
                    handleWarnings(cs);
                    return (T) result;
                } catch (SQLException ex) {
                    if (csc instanceof ParameterDisposer) {
                        ((ParameterDisposer) csc).cleanupParameters();
                    }
                    String sql = null;
                    if (csc instanceof SqlProvider) {
                        sql = ((SqlProvider) csc).getSql();
                    }
                    throw getExceptionTranslator().translate("CallableStatementCallback", sql, ex);
                } finally {
                    if (csc instanceof ParameterDisposer) {
                        ((ParameterDisposer) csc).cleanupParameters();
                    }
                    JdbcUtils.closeStatement(cs);
                }
            }

        });

    }

    @Override
    public void afterPropertiesSet() {
        if (!(isLazyInit()))
            getExceptionTranslator();
        // super.afterPropertiesSet();
    }

    @Override
    protected void applyStatementSettings(Statement stmt) throws SQLException {
        int fetchSize = getFetchSize();
        if (fetchSize > 0) {
            stmt.setFetchSize(fetchSize);
        }
        int maxRows = getMaxRows();
        if (maxRows > 0) {
            stmt.setMaxRows(maxRows);
        }
        if (getQueryTimeout() > 0) {
            stmt.setQueryTimeout(getQueryTimeout());
        }
    }

}
