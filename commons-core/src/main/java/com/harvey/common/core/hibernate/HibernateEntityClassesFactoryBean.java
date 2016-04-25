package com.harvey.common.core.hibernate;

import java.util.List;

import org.springframework.beans.factory.FactoryBean;

import com.harvey.common.core.utils.ResourceUtils;

public class HibernateEntityClassesFactoryBean implements FactoryBean<Class<?>[]> {
	private String basePackages;
	
	@Override
	public Class<?>[] getObject() throws Exception {
		List<Class<?>> classes = ResourceUtils.getClasses(basePackages, "*Model.class");
		List<Class<?>> itemClazzs = ResourceUtils.getClasses(basePackages, "*Item.class");
		classes.addAll(itemClazzs);
		return classes.toArray(new Class<?>[classes.size()]);
	}

	@Override
	public Class<?> getObjectType() {
		return new Class<?>[]{}.getClass();
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
