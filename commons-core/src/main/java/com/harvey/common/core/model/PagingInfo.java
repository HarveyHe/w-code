package com.harvey.common.core.model;

import java.io.Serializable;

public class PagingInfo implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int pageSize;
    
    private int pageNo;
    
    private int totalRows;
    
    public PagingInfo() {
    	this(10,1);
    }
    
    public PagingInfo(int pageSize, int pageNo) {
        this.pageSize = pageSize;
        this.pageNo = pageNo;
    }
    
    public int getCurrentRow() {
        if (pageNo <= 0) {
            return 0;
        }
        return pageSize * (pageNo - 1);
    }
    
    public int getTotalPages() {
        if (totalRows <= 0 || pageSize <= 0) {
            return 0;
        }
        return (totalRows - 1) / pageSize + 1;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int currentPage) {
        this.pageNo = currentPage;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    @Override
    public String toString() {
        return "PagingInfo [pageSize=" + pageSize + ", pageNo=" + pageNo + ", totalRows=" + totalRows + "]";
    }
}
