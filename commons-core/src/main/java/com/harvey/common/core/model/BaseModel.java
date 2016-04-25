package com.harvey.common.core.model;

import java.io.Serializable;
import java.util.List;

public interface BaseModel extends Serializable {
	ModelState getModelState();

	void setModelState(ModelState modelState);

	void addValidField(String fieldName);

	List<String> validFields();

	Serializable primeryKeyValue();

}
