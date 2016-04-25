package com.harvey.common.core.context;

import com.harvey.common.core.model.UserBaseModel;

public interface CurrentUserDelegate {
    <T extends UserBaseModel> T getCurrentUser();
}
