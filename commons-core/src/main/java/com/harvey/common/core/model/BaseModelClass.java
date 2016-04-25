package com.harvey.common.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.harvey.common.core.dao.utils.EntityUtils;

public abstract class BaseModelClass implements BaseModel {

    private ModelState modelState;
    private List<String> validFields = new ArrayList<String>();
    @Override
    public ModelState getModelState() {
        return modelState;
    }

    @Override
    public void setModelState(ModelState modelState) {
        this.modelState = modelState; 
    }

    @Override
    public void addValidField(String fieldName) {
        validFields.add(fieldName);
    }


	@Override
    public List<String> validFields() {
        return validFields;
    }

	@Override
	public Serializable primeryKeyValue() {
	   return EntityUtils.getId(this);
    }
	
}
