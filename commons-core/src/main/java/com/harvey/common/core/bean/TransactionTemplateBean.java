package com.harvey.common.core.bean;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.harvey.common.core.context.Context;

public class TransactionTemplateBean extends TransactionTemplate {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TransactionTemplateBean() {
        this(Context.getBean(PlatformTransactionManager.class));
        this.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
    }

    public TransactionTemplateBean(PlatformTransactionManager transactionManager, TransactionDefinition transactionDefinition) {
        super(transactionManager, transactionDefinition);
    }

    public TransactionTemplateBean(PlatformTransactionManager transactionManager) {
        super(transactionManager);
    }
    
}
