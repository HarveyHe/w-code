package com.harvey.common.core.model;

import java.util.Date;

public interface ModifyingInfo {
    String getModifier();
    
    void setModifier(String modifier);
    
    Date getModifyTime();
    
    void setModifyTime(Date modifyTime);
}
