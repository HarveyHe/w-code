package com.harvey.common.core.hibernate;

import java.util.List;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

import com.harvey.common.core.utils.ResourceUtils;


public class HibernateMappingLocationsFactoryBean implements FactoryBean<Resource[]> {
	private String basePackages;
	
	@Override
	public Resource[] getObject() throws Exception {
		List<Resource> resources = ResourceUtils.getResources(basePackages, "*.hbm.xml");
		return resources.toArray(new Resource[resources.size()]);
	}

	@Override
	public Class<?> getObjectType() {
		return new Resource[]{}.getClass();
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public String getBasePackages() {
		return basePackages;
	}

	public void setBasePackages(String basePackages) {
		this.basePackages = basePackages;
	}


}
