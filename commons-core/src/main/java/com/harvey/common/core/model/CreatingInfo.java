package com.harvey.common.core.model;

import java.util.Date;

public interface CreatingInfo {
    
    String getCreator();
    
    void setCreator(String creator);
    
    Date getCreateTime();
    
    void setCreateTime(Date createTime);
}
