package com.harvey.common.core.dao.utils;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.harvey.common.core.bean.DefaultBeanWrapper;
import com.harvey.common.core.model.BaseModel;

public class EntityUtils {

	public static Class<?> getEntityClass(Class<?> entityClass) {
		Class<?> clazz = entityClass;
		while (clazz != null && !clazz.isAnnotationPresent(Entity.class)) {
			clazz = clazz.getSuperclass();
		}
		if (clazz == null) {
			return entityClass;
		} else {
			return (Class<?>) clazz;
		}
	}

	public static <TModel> TModel convertEntityType(Object entity, Class<TModel> type) {
		if(entity == null){
		    return null;
		}else if (type.isAssignableFrom(entity.getClass())) {
			return (TModel) entity;
		} else {
			try {
				TModel result = type.newInstance();
				PropertyUtils.copyProperties(result, entity);
				// BeanUtils.copyProperties(entity, result);
				return result;
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static String getTableName(Class<? extends BaseModel> entityClass) {
		entityClass = (Class<? extends BaseModel>) getEntityClass(entityClass);
		if (entityClass.isAnnotationPresent(Table.class)) {
			return entityClass.getAnnotation(Table.class).name();
		} else {
			return null;
		}
	}

	public static int getFieldCount(Class<?> entityClass) {
		int count = 0;
		for (Method method : entityClass.getMethods()) {
			if (method.isAnnotationPresent(Column.class)) {
				count++;
			}
		}
		return count;
	}

	public static List<String> getFieldNames(Class<?> entityClass) {
		List<String> fieldNames = new ArrayList<String>();
		DefaultBeanWrapper beanWrapper = new DefaultBeanWrapper(entityClass);
		for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
			if (beanWrapper.isReadableProperty(propertyDescriptor.getName()) && propertyDescriptor.getReadMethod().isAnnotationPresent(Column.class)) {
				fieldNames.add(propertyDescriptor.getName());
			}
		}
		return fieldNames;
	}

	public static String getIdFieldName(Class<?> entityClass) {
		/*
		 * for (Method method : entityClass.getMethods()) { if
		 * (method.isAnnotationPresent(Id.class)) { String methodName =
		 * method.getName(); return methodName.substring(3, 4).toLowerCase() +
		 * methodName.substring(4); } }
		 */
	    entityClass = EntityUtils.getEntityClass(entityClass);
		DefaultBeanWrapper beanWrapper = new DefaultBeanWrapper(entityClass);
		for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
			if (propertyDescriptor.getReadMethod() != null && propertyDescriptor.getReadMethod().isAnnotationPresent(Id.class)) {
				return propertyDescriptor.getName();
			}
		}
		return null;
	}

	public static Class<?> getIdFieldType(Class<?> entityClass) {
	    entityClass = EntityUtils.getEntityClass(entityClass);
		DefaultBeanWrapper beanWrapper = new DefaultBeanWrapper(entityClass);
		for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
			if (propertyDescriptor.getReadMethod() != null && propertyDescriptor.getReadMethod().isAnnotationPresent(Id.class)) {
				return propertyDescriptor.getPropertyType();
			}
		}
		return null;
	}

	public static String getIdFieldNameByConditionObject(Object conditionObject) {
		Class<?> itemClass = QueryUtils.getItemClassByConditionObject(conditionObject);
		if (itemClass != null && BaseModel.class.isAssignableFrom(itemClass)) {
			return getIdFieldName(itemClass);
		} else {
			return null;
		}
	}

	public static void setId(Object entity, Serializable id) {
	    Class<?> entityClass = EntityUtils.getEntityClass(entity.getClass());
	      DefaultBeanWrapper beanWrapper = new DefaultBeanWrapper(entityClass);
		for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
			if (propertyDescriptor.getReadMethod() != null && propertyDescriptor.getWriteMethod() != null
			        && propertyDescriptor.getReadMethod().isAnnotationPresent(Id.class)) {
			    beanWrapper.setWrappedInstance(entity);
			    beanWrapper.setPropertyValue(propertyDescriptor.getName(), id);
			    break;
			}
		}
	}

	public static Serializable getId(Object entity) {
	    Class<?> entityClass = EntityUtils.getEntityClass(entity.getClass());
		if (entityClass.isAnnotationPresent(IdClass.class)) {
			Class<?> idClass = entityClass.getAnnotation(IdClass.class).value();
			try {
				Serializable id = (Serializable) idClass.newInstance();
				BeanUtils.copyProperties(entity, id);
				// /PropertyUtils.copyProperties(result, entity
				return id;
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		DefaultBeanWrapper beanWrapper = new DefaultBeanWrapper(entityClass);
		for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
			if (propertyDescriptor.getReadMethod() != null && propertyDescriptor.getReadMethod().isAnnotationPresent(Id.class)) {
				try {
				    beanWrapper.setWrappedInstance(entity);
					return (Serializable) beanWrapper.getPropertyValue(propertyDescriptor.getName());
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}
		return null;
	}

	public static String getNormalName(String name) {
		int index = name.indexOf('@');
		if (index > -1) {
			return name.substring(0, index);
		}else if((index = name.indexOf("_$$_")) > -1){
		    return name.substring(0, index);
		}
		return name;
	}

	public static String toColumnName(String fieldName) {
		if (StringUtils.isEmpty(fieldName)) {
			return fieldName;
		}
		StringBuilder sb = new StringBuilder(fieldName);
		int i = 1;
		while (i < sb.length()) {
			char ch = sb.charAt(i);
			if (Character.isUpperCase(ch)) {
				sb.setCharAt(i, Character.toLowerCase(ch));
				sb.insert(i, '_');
				i++;
			}
			i++;
		}
		return sb.toString();
	}

	public static String toPascalCase(String source, boolean bTitleCase) {
		if (source == null || source.trim().length() == 0) {
			return "";
		}
		if (source.indexOf("_") >= 0 || source.equals(source.toUpperCase())) {
			source = source.toLowerCase();
		}
		String[] parts = source.split("_");
		StringBuilder sb = new StringBuilder();
		for (String part : parts) {
			if (part.length() == 0) {
				continue;
			}
			sb.append(part.substring(0, 1).toUpperCase());
			sb.append(part.substring(1));
		}
		if (bTitleCase) {
			return sb.toString();
		} else {
			return sb.substring(0, 1).toLowerCase() + sb.substring(1);
		}
	}

	public static boolean isParamValid(Object value) {
		if (value == null) {
			return false;
		}
		if (value instanceof String) {
			if (((String) value).trim().length() == 0) {
				return false;
			}
		}
		if (value.getClass().isArray()) {
			if (ArrayUtils.getLength(value) == 0 || Array.get(value, 0) == null) {
				return false;
			}
		}
		if (value instanceof Collection) {
			if (((Collection<?>) value).isEmpty() || ((Collection<?>) value).iterator().next() == null) {
				return false;
			}
		}
		return true;
	}
}
