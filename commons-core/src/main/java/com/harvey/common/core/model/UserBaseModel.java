package com.harvey.common.core.model;

import java.io.Serializable;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserBaseModel extends UserDetails {

    /**
     * 获取User Id
     * @return
     */
    Serializable primeryKeyValue();
    
    /**
     * 储存附加数据
     * @return
     */
    Map<Object,Object> getAttribute();
}
