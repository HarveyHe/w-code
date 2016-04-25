package com.harvey.common.core.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseItemClass implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String rowUUUId;

    @Id
    @Column(name = "UUID__")
    public String getRowUuuId() {
        return rowUUUId;
    }

    public void setRowUuuId(String uuId) {
        this.rowUUUId = uuId;
    }
}
