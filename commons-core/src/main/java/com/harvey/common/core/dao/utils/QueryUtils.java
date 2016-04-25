package com.harvey.common.core.dao.utils;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.beetl.core.Template;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.query.spi.ParamLocationRecognizer;
import org.hibernate.engine.query.spi.ParamLocationRecognizer.NamedParameterDescription;
import org.hibernate.engine.query.spi.ParameterMetadata;
import org.hibernate.engine.query.spi.ParameterParser;
import org.hibernate.engine.spi.NamedSQLQueryDefinition;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import com.harvey.common.core.bean.DefaultBeanWrapper;
import com.harvey.common.core.context.Context;
import com.harvey.common.core.dao.query.QueryTemplate;
import com.harvey.common.core.model.BaseModel;
import com.harvey.common.core.model.PagingInfo;

public class QueryUtils {
    private static final String NAMED_PARAMETER_PREFIX = "$param$_";
    public static final String SQL_EXTRA_CONDITIONS_MACRO = "##CONDITIONS##";
    private static Set<Integer> SQL_SEPARATORSET;
    static {
        SQL_SEPARATORSET = new HashSet<Integer>();
        for (char c : "     \n\r\f\t,:()=<>$&|+-=/*'^![]#~\\".toCharArray()) {
            SQL_SEPARATORSET.add((int) c);
        }
    }

    public static String getSqlQueryName(Class<?> conditionClass) {
        String queryName = EntityUtils.getNormalName(conditionClass.getSimpleName());
        int index = queryName.indexOf("QueryCondition_$$_");
        if (index > -1) {
            queryName = queryName.substring(0, index);
        } else if (queryName.endsWith("QueryCondition")) {
            queryName = queryName.substring(0, queryName.length() - 9);
        }
        return queryName;
    }

    public static String getSqlQueryName(Object condition) {
        return getSqlQueryName(condition.getClass());
    }

    public static Class<?> getQueryItemClass(Object condition) {
        String queryConditionClassName = EntityUtils.getNormalName(condition.getClass().getName());
        if (queryConditionClassName.endsWith("QueryCondition")) {
            String queryItemClassName = queryConditionClassName.substring(0, queryConditionClassName.length() - 9) + "Item";
            try {
                return (Class<?>) Class.forName(queryItemClassName);
            } catch (ClassNotFoundException cnfe) {
                throw new RuntimeException(cnfe);
            }
        } else {
            throw new RuntimeException("Item class for " + queryConditionClassName + " not found");
        }
    }

    public static Class<?> getItemClassByConditionObject(Object conditionObject) {
        if (conditionObject instanceof String) {
            try {
                return Class.forName((String) conditionObject);
            } catch (ClassNotFoundException cnfex) {
                return null;
            }
        } else if (conditionObject instanceof Class) {
            return (Class<?>) conditionObject;
        } else if (conditionObject instanceof BaseModel) {
            return conditionObject.getClass();
        } else {
            return getQueryItemClass(conditionObject);
        }
    }

    public static String getSqlUpdateName(Object condition) {
        String updateName = EntityUtils.getNormalName(condition.getClass().getSimpleName());
        if (updateName.endsWith("UpdateCondition")) {
            updateName = updateName.substring(0, updateName.length() - 9);
        }
        return updateName;
    }

    public static Order[] parseOrderByToHibernateOrders(String orderBy) {
        if (orderBy == null || orderBy.trim().length() == 0) {
            return null;
        }
        List<Order> result = new ArrayList<Order>();
        String[] orders = orderBy.split(",");
        for (String order : orders) {
            order = order.trim();
            if (order.length() == 0) {
                continue;
            }
            if (order.toLowerCase().endsWith(" asc")) {
                result.add(Order.asc(EntityUtils.toPascalCase(order.substring(0, order.length() - 4).trim(), false)));
            } else if (order.toLowerCase().endsWith(" desc")) {
                result.add(Order.desc(EntityUtils.toPascalCase(order.substring(0, order.length() - 5).trim(), false)));
            } else {
                result.add(Order.asc(EntityUtils.toPascalCase(order.trim(), false)));
            }
        }
        return result.toArray(new Order[result.size()]);
    }

    public static void addOrderBy(Criteria criteria, String orderBy) {
        if (orderBy != null && orderBy.trim().length() != 0) {
            Order[] orders = parseOrderByToHibernateOrders(orderBy);
            for (Order order : orders) {
                criteria.addOrder(order);
            }
        }
    }

    public static String addOrderBy(String sql, String orderBy) {
        if (orderBy != null && orderBy.trim().length() != 0) {
            StringBuilder sb = new StringBuilder("SELECT * FROM (");
            sb.append(sql).append(") T_ order by ").append(orderBy);
            return sb.toString();
        } else {
            return sql;
        }
    }

    public static void setPagingInfo(Criteria criteria, PagingInfo pagingInfo) {
        if (pagingInfo != null) {
            if (pagingInfo.getPageSize() <= 0) {
                pagingInfo.setPageSize(10);
            }
            if (pagingInfo.getPageNo() > pagingInfo.getTotalPages()) {
                pagingInfo.setPageNo(pagingInfo.getTotalPages());
            }
            if (pagingInfo.getPageNo() <= 0) {
                pagingInfo.setPageNo(1);
            }
            criteria.setFirstResult(pagingInfo.getCurrentRow());
            criteria.setMaxResults(pagingInfo.getPageSize());
        }
    }

    public static void setPagingInfo(Query query, PagingInfo pagingInfo) {
        if (pagingInfo != null) {
            if (pagingInfo.getPageSize() <= 0) {
                pagingInfo.setPageSize(10);
            }
            if (pagingInfo.getPageNo() > pagingInfo.getTotalPages()) {
                pagingInfo.setPageNo(pagingInfo.getTotalPages());
            }
            if (pagingInfo.getPageNo() <= 0) {
                pagingInfo.setPageNo(1);
            }
            query.setFirstResult(pagingInfo.getCurrentRow());
            query.setMaxResults(pagingInfo.getPageSize());
        }
    }

    public static void addSqlCondtion(SessionFactory sessionFactory, Criteria criteria, String sqlCondition, Object[] parameterValues) {
        if (sqlCondition == null || sqlCondition.trim().length() == 0) {
            return;
        }
        if (parameterValues == null || parameterValues.length == 0) {
            criteria.add(Restrictions.sqlRestriction(sqlCondition));
        } else {
            boolean hasArrayParameters = false;
            for (Object value : parameterValues) {
                if (value != null && value.getClass().isArray()) {
                    hasArrayParameters = true;
                    break;
                }
            }
            if (hasArrayParameters) {
                int[] parameterLocations = getOrdinalParameterLocations(sessionFactory, sqlCondition);
                StringBuilder sqlConditionSb = new StringBuilder(sqlCondition);
                List<Object> parameterValuesList = new ArrayList<Object>();
                for (int i = parameterValues.length - 1; i >= 0; i--) {
                    Object value = parameterValues[i];
                    if (value != null && value.getClass().isArray() && Array.getLength(value) == 0) {
                        value = null;
                    }
                    if (value != null && value.getClass().isArray()) {
                        int size = Array.getLength(value);
                        if (size > 1) {
                            sqlConditionSb.insert(parameterLocations[i], StringUtils.repeat("?, ", size - 1));
                        }
                        for (int j = size - 1; j >= 0; j--) {
                            parameterValuesList.add(0, Array.get(value, j));
                        }
                    } else {
                        parameterValuesList.add(0, value);
                    }
                }
                parameterValues = parameterValuesList.toArray();
                sqlCondition = sqlConditionSb.toString();
            }

            Type[] parameterTypes = getParameterTypes(parameterValues);
            criteria.add(Restrictions.sqlRestriction(sqlCondition, parameterValues, parameterTypes));
        }
    }

    public static int[] getOrdinalParameterLocations(SessionFactory sessionFactory, String sql) {
        ParameterMetadata parameterMetaData = ((SessionFactoryImplementor) sessionFactory).getQueryPlanCache().getSQLParameterMetadata(sql);
        int count = parameterMetaData.getOrdinalParameterCount();
        int[] ordinalParameterLocations = new int[count];
        for (int i = 0; i < ordinalParameterLocations.length; i++) {
            ordinalParameterLocations[i] = parameterMetaData.getOrdinalParameterSourceLocation(i + 1);
        }
        return ordinalParameterLocations;
    }

    public static Type[] getParameterTypes(Object[] parameterValues) {
        Type[] parameterTypes = new Type[parameterValues.length];
        for (int i = 0; i < parameterValues.length; i++) {
            Object value = parameterValues[i];
            if (value instanceof String) {
                parameterTypes[i] = StandardBasicTypes.STRING;
            } else if (value instanceof Double) {
                parameterTypes[i] = StandardBasicTypes.DOUBLE;
            } else if (value instanceof Integer) {
                parameterTypes[i] = StandardBasicTypes.INTEGER;
            } else if (value instanceof Long) {
                parameterTypes[i] = StandardBasicTypes.LONG;
            } else if (value instanceof Date) {
                parameterTypes[i] = StandardBasicTypes.TIMESTAMP;
            } else {
                parameterTypes[i] = StandardBasicTypes.STRING;
            }
        }
        return parameterTypes;
    }

    public static void addExample(Criteria criteria, Object exampleEntity) {
        DefaultBeanWrapper exampleWrapper = new DefaultBeanWrapper(exampleEntity);
        // PropertyDescriptor[] propertyDescriptors =
        // exampleWrapper.getPropertyDescriptors();
        List<String> fieldNames = EntityUtils.getFieldNames((Class<? extends BaseModel>) exampleEntity.getClass());
        for (String fieldName : fieldNames) {
            Object value = exampleWrapper.getPropertyValue(fieldName);
            if (EntityUtils.isParamValid(value)) {
                criteria.add(Restrictions.eq(fieldName, value));
            }
        }
    }

    public static String toNamedParameters(SessionFactory sessionFactory, String sql) {
        if (sql == null || sql.trim().length() == 0) {
            return sql;
        }
        int[] ordinalParameterLocations = getOrdinalParameterLocations(sessionFactory, sql);
        StringBuilder resultSql = new StringBuilder(sql);
        for (int i = ordinalParameterLocations.length - 1; i >= 0; i--) {
            int location = ordinalParameterLocations[i];
            resultSql.delete(location, location + 1);
            resultSql.insert(location, ":" + NAMED_PARAMETER_PREFIX + i);
        }
        return resultSql.toString();
    }

    public static Map<String, Object> toNamedParameters(Object[] parameterValues) {
        Map<String, Object> namedParameters = new HashMap<String, Object>();
        for (int i = 0; i < parameterValues.length; i++) {
            Object value = parameterValues[i];
            if (value == null) {
                value = "";
            }
            if (value.getClass().isArray()) {
                List<Object> notNullArrayItems = new ArrayList<Object>();
                for (int j = 0; j < Array.getLength(value); j++) {
                    Object itemValue = Array.get(value, j);
                    if (itemValue != null) {
                        notNullArrayItems.add(itemValue);
                    }
                }
                value = notNullArrayItems.toArray();
                if (Array.getLength(value) == 0) {
                    value = "";
                }
            }
            namedParameters.put(NAMED_PARAMETER_PREFIX + i, value);
        }
        return namedParameters;
    }

    public static Map<String, Object> toNamedParameters(Object valueBean) {
        if (valueBean instanceof Map<?, ?>) {
            return (Map<String, Object>) valueBean;
        }
        DefaultBeanWrapper beanWrapper = new DefaultBeanWrapper(valueBean);
        PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();
        Map<String, Object> namedParameters = new HashMap<String, Object>();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String propertyName = propertyDescriptor.getName();
            if ("class".equals(propertyName)) {
                continue;
            }
            Object value = beanWrapper.getPropertyValue(propertyName);
            if (value == null) {
                value = "";
            }
            if (value.getClass().isArray()) {
                List<Object> notNullArrayItems = new ArrayList<Object>();
                for (int i = 0; i < Array.getLength(value); i++) {
                    Object itemValue = Array.get(value, i);
                    if (itemValue != null) {
                        notNullArrayItems.add(itemValue);
                    }
                }
                value = notNullArrayItems.toArray();
                if (Array.getLength(value) == 0) {
                    value = "";
                }
            }
            namedParameters.put(propertyName, value);
        }
        return namedParameters;
    }

    public static String addExtraConditions(String sql, String sqlCondition) {
        if (sqlCondition != null && sqlCondition.trim().length() != 0) {
            sqlCondition = "(" + sqlCondition + ")";
            if (sql.indexOf(SQL_EXTRA_CONDITIONS_MACRO) >= 0) {
                sql = sql.replace(SQL_EXTRA_CONDITIONS_MACRO, sqlCondition);
            } else {
                sql = "SELECT T__CONDITION__.* FROM (" + sql + ") T__CONDITION__ WHERE " + sqlCondition;
            }
        } else {
            sql = sql.replace(SQL_EXTRA_CONDITIONS_MACRO, "0=0");
        }
        return sql;
    }

    public static String getNamedSql(SessionFactory sessionFactory, String sqlName) {
        NamedSQLQueryDefinition sqlQuery = ((SessionFactoryImpl) sessionFactory).getNamedSQLQuery(sqlName);
        if (sqlQuery != null) {
            return parseNameSql(sqlQuery.getQueryString());
        } else {
            throw new RuntimeException("Query " + sqlName + " not found");
        }
    }

    public static String parseNameSql(String sql) {
        StringBuilder sb = new StringBuilder();
        StringReader reader = new StringReader(sql);
        int c;
        int c1;
        try {
            while ((c = reader.read()) != -1) {
                if (c == '<') {
                    reader.mark(1);
                    c1 = reader.read();
                    if (c1 != '<') {
                        reader.reset();
                        sb.append((char) c);
                        continue;
                    }
                    // 读取 << >>的内容
                    Set<String> argNames = new HashSet<String>();
                    StringBuilder buff = new StringBuilder();
                    while ((c = reader.read()) != -1) {
                        if (c == ':') {
                            buff.append(':');
                            StringBuilder argName = new StringBuilder();
                            while ((c = reader.read()) != -1) {
                                if (!SQL_SEPARATORSET.contains(c)) {
                                    buff.append((char) c);
                                    argName.append((char) c);
                                    reader.mark(1);
                                    c = reader.read();
                                    reader.reset();
                                    if (SQL_SEPARATORSET.contains(c) || c == -1) {
                                        argNames.add(argName.toString());
                                        break;
                                    }
                                }
                            }
                            continue;
                        }
                        if (c == '>') {
                            reader.mark(1);
                            c1 = reader.read();
                            if (c1 == '>') {
                                break;
                            }
                            reader.reset();
                        }
                        buff.append((char) c);
                    }
                    if (argNames.size() > 0) {// 转换为:<%if(!isEmpty(arg.name))
                                              // {%> <%}%>
                        sb.append("<%if(!(");
                        Iterator<String> iterator = argNames.iterator();
                        for (; iterator.hasNext();) {
                            sb.append("isEmpty(arg.").append(iterator.next()).append(')');
                            if (iterator.hasNext()) {
                                sb.append("&&");
                            }
                        }
                        sb.append(")){%>")
                          //.append(LINE_SEPARATOR)
                          .append(buff)
                          //.append(LINE_SEPARATOR)
                          .append("${''}<%}%>");
                    } else {
                        sb.append(buff);
                    }
                } else {
                    sb.append((char) c);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        return sb.toString();
    }

    public static String trimLine(String source){
        return trimLine(new StringBuffer(source));
    }
    
    public static String trimLine(StringBuffer buff){
        int start = 0,current = 0;
        boolean isEmptyLine = true;
        while(current < buff.length()){
            char c = buff.charAt(current);
            if( c == '\n'){
                if(isEmptyLine){
                    buff.delete(start,current + 1);
                    current = start;
                    continue;
                }else{
                    start = current + 1;
                }
                isEmptyLine = true;
            }else if(isEmptyLine && !Character.isWhitespace(c)){
                isEmptyLine = false;
            }
            current++;
        }
        if(buff.charAt(buff.length() - 1) == '\n'){
            buff.deleteCharAt(buff.length() - 1);
        }
        if(buff.charAt(buff.length() - 1) == '\r'){
            buff.deleteCharAt(buff.length() - 1);
        }
        return  buff.toString();
    }
    
    public static String getDynamicSql(SessionFactory sessionFactory, String sqlName, Map<String,Object> valueBean) {
        Template template = QueryTemplate.getQueryTemplate(sessionFactory).getTemplate(sqlName);
        template.binding("arg", valueBean);
        template.binding("user", Context.getCurrentUser());
        StringWriter writer = new StringWriter();
        template.renderTo(writer);
        return trimLine(writer.getBuffer());        
    }
    
    public static String getDynamicSql(SessionFactory sessionFactory, String sqlName, Object valueBean) {
        Map<String,Object> map = toNamedParameters(valueBean);
        return getDynamicSql(sessionFactory,sqlName,map);
    }

    /*private static Pattern dynamicConditionPattern = Pattern.compile("<<.*?>>");

    @Deprecated
    public static String getDynamicNamedSql(SessionFactory sessionFactory, String sqlName, Object valueBean) {
        String sql = getNamedSql(sessionFactory, sqlName);

        DefaultBeanWrapper valueBeanWrapper = new DefaultBeanWrapper(valueBean);
        StringBuffer sb = new StringBuffer();
        Matcher dynamicConditionMatcher = dynamicConditionPattern.matcher(sql);
        while (dynamicConditionMatcher.find()) {
            String dynamicCondition = dynamicConditionMatcher.group();
            boolean parametersValid = false;
            for (String parameterName : getSqlParameters(sessionFactory, dynamicCondition).values()) {
                if (EntityUtils.isParamValid(valueBeanWrapper.getPropertyValue(parameterName))) {
                    parametersValid = true;
                } else {
                    parametersValid = false;
                    break;
                }
            }
            if (parametersValid) {
                dynamicConditionMatcher.appendReplacement(sb, dynamicCondition.substring(2, dynamicCondition.length() - 2));
            } else {
                dynamicConditionMatcher.appendReplacement(sb, "");
            }
        }
        dynamicConditionMatcher.appendTail(sb);
        sql = sb.toString();

        sql = sql.replaceAll("([ \t]*\n\t*)+", "\n");
        return sql;
    }

    @Deprecated
    public static String getDynamicNamedSql(SessionFactory sessionFactory, String sqlName, Map<String, Object> parameters) {
        String sql = getNamedSql(sessionFactory, sqlName);
        StringBuffer sb = new StringBuffer();
        Matcher dynamicConditionMatcher = dynamicConditionPattern.matcher(sql);
        while (dynamicConditionMatcher.find()) {
            String dynamicCondition = dynamicConditionMatcher.group();
            boolean parametersValid = false;
            for (String parameterName : getSqlParameters(sessionFactory, dynamicCondition).values()) {
                if (parameters != null && EntityUtils.isParamValid(parameters.get(parameterName))) {
                    parametersValid = true;
                } else {
                    parametersValid = false;
                    break;
                }
            }
            if (parametersValid) {
                dynamicConditionMatcher.appendReplacement(sb, dynamicCondition.substring(2, dynamicCondition.length() - 2));
            } else {
                dynamicConditionMatcher.appendReplacement(sb, "");
            }
        }
        dynamicConditionMatcher.appendTail(sb);
        sql = sb.toString();

        sql = sql.replaceAll("([ \t]*\n\t*)+", "\n");
        return sql;
    }*/

    public static TreeMap<Integer, String> getSqlParameters(SessionFactory sessionFactory, String sql) {
        ParameterMetadata parameterMetaData = ((SessionFactoryImplementor) sessionFactory).getQueryPlanCache().getSQLParameterMetadata(sql);
        Set<String> parameterNames = parameterMetaData.getNamedParameterNames();

        TreeMap<Integer, String> parameters = new TreeMap<Integer, String>();
        for (String parameterName : parameterNames) {
            int[] locations = parameterMetaData.getNamedParameterSourceLocations(parameterName);
            for (int location : locations) {
                parameters.put(location, parameterName);
            }
        }
        return parameters;
    }

    public static TreeMap<Integer, String> getSqlParameters(String sql) {
        ParamLocationRecognizer recognizer = new ParamLocationRecognizer();
        ParameterParser.parse(sql, recognizer);
        TreeMap<Integer, String> map = new TreeMap<Integer, String>();
        for (Map.Entry<String, NamedParameterDescription> entry : recognizer.getNamedParameterDescriptionMap().entrySet()) {
            int[] positions = entry.getValue().buildPositionsArray();
            for (int pos : positions) {
                map.put(pos, entry.getKey());
            }
        }
        return map;
    }

    public static Set<String> getSqlParameterSets(String sql) {
        ParamLocationRecognizer recognizer = new ParamLocationRecognizer();
        ParameterParser.parse(sql, recognizer);
        return recognizer.getNamedParameterDescriptionMap().keySet();
    }
}
