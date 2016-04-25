package com.harvey.common.core.bean;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;

import com.harvey.common.core.dao.utils.QueryUtils;

/**
 * 储存过程的名称如:sp_test或pkg.sp_test
 * 
 * @author admin
 * 
 */
public class StoredProcedureBean extends StoredProcedure {
    private static final TypeConverter typeConverter = new SimpleTypeConverter();
    private static final Map<String, Integer> SqlTypesMap = new HashMap<String, Integer>();

    private static Map<String, Integer> getSqlTypesMap() {
        if (SqlTypesMap.size() == 0) {
            synchronized (SqlTypesMap) {
                Map<String, Integer> sqlTypesMap = new HashMap<String, Integer>();
                for (Field field : Types.class.getDeclaredFields()) {
                    if (Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
                        field.setAccessible(true);
                        try {
                            sqlTypesMap.put(field.getName(), field.getInt(null));
                        } catch (Exception ex) {
                        }
                    }
                }
                SqlTypesMap.putAll(sqlTypesMap);
            }
        }
        return SqlTypesMap;
    }

    private Map<String, Object> parameterValues = new LinkedHashMap<String, Object>();

    public StoredProcedureBean() {
        super();
    }

    public StoredProcedureBean(SessionFactory sessionFactory) {
        JdbcTemplateBean jdbcTemplate = new JdbcTemplateBean(sessionFactory);
        // jdbcTemplate.setDataSource(Utils.getDataSource(sessionFactory));
        setJdbcTemplate(jdbcTemplate);
    }

    public void addInParameter(String name, Object value, int sqlType) {
        parameterValues.put(name, value);
        SqlParameter param = new SqlParameter(name, sqlType);
        super.declareParameter(param);
    }

    public void addInOutParameter(String name, Object value, int sqlType) {
        parameterValues.put(name, value);
        SqlParameter param = new SqlInOutParameter(name, sqlType);
        super.declareParameter(param);
    }

    public void addOutParameter(String name, int sqlType) {
        SqlParameter param = new SqlOutParameter(name, sqlType);
        super.declareParameter(param);
    }

    public Map<String, Object> execute() throws DataAccessException {
        return super.execute(parameterValues);
    }

    public static int resolverSqlType(String fieldType) {
        int idx = fieldType.lastIndexOf('.');
        String clazzStr = fieldType.substring(0, idx);
        String fieldName = fieldType.substring(idx + 1, fieldType.length());
        try {
            Class<?> clazz = Class.forName(clazzStr);
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(null);
        } catch (Exception ex) {

        }
        return Types.NULL;
    }

    public static int getSqlType(String typeStr) {
        if ("string".contentEquals(typeStr)) {
            return Types.NVARCHAR;
        }
        Map<String, Integer> sqlTypesMap = getSqlTypesMap();
        for (Entry<String, Integer> entry : sqlTypesMap.entrySet()) {
            if (StringUtils.containsIgnoreCase(entry.getKey(), typeStr)) {
                return entry.getValue();
            }
        }
        if (typeStr != null && typeStr.indexOf('.') > 0) {
            return resolverSqlType(typeStr);
        }
        try {
            return Integer.parseInt(typeStr);
        } catch (Exception ex) {

        }
        return Types.VARCHAR;
    }

    public static Class<?> sqlTypeToJavaType(int sqlType) {
        switch (sqlType) {
        case Types.DECIMAL:
        case Types.DOUBLE:
        case Types.NUMERIC:
        case Types.FLOAT:
        case Types.REAL:
            return Double.class;
        case Types.TINYINT:
        case Types.BIT:
            return Byte.class;
        case Types.INTEGER:
            return Integer.class;
        case Types.BIGINT:
            return Long.class;
        case Types.TIMESTAMP:
        case Types.DATE:
        case Types.TIME:
            return Date.class;
        case Types.BLOB:
        case Types.LONGVARBINARY:
        case Types.BINARY:
            return byte[].class;
        default:
            return String.class;
        }
    }

    /**
     * 支持的格式为: sp_test(uid IN int,count OUT int)
     */
    public static StoredProcedureBean parseProcedure(String spName, Object param, StoredProcedureBean procedure) throws Exception {

        int index = spName.indexOf('(');
        if (index <= 0) {
            throw new IllegalArgumentException("Unkown storedprocedure format:" + spName);
        }
        int end = spName.lastIndexOf(')');
        if (end <= 0) {
            throw new IllegalArgumentException("Unkown storedprocedure format:" + spName);
        }
        procedure.setSql(spName.substring(0, index));
        // uid IN int,count OUT int
        Map<String, Object> paramMap = QueryUtils.toNamedParameters(param);

        String paramInfos = spName.substring(index + 1, end);
        for (String paramInfo : StringUtils.split(paramInfos, ',')) {
            String[] paramItem = StringUtils.split(paramInfo, ' ');
            if (paramItem.length < 1 || paramItem.length > 3) {
                continue;
            }
            String name, pType, typeStr;
            if (paramItem.length == 2) {
                name = paramItem[0];
                typeStr = paramItem[1];
                pType = "in";
            } else {
                name = paramItem[0];
                pType = paramItem[1];
                typeStr = paramItem[2];
            }
            int sqlType = StoredProcedureBean.getSqlType(typeStr);
            if ("in".equalsIgnoreCase(pType) || "inout".equalsIgnoreCase(pType)) {
                Class<?> paramType = StoredProcedureBean.sqlTypeToJavaType(sqlType);
                Object paramValue = getParamValue(name, paramMap);
                try {
                    paramValue = typeConverter.convertIfNecessary(paramValue, paramType);
                } catch (Exception ex) {

                }
                if ("in".equalsIgnoreCase(pType)) {
                    procedure.addInParameter(name, paramValue, sqlType);
                } else {
                    procedure.addInOutParameter(name, paramValue, sqlType);
                }
            } else {
                procedure.addOutParameter(name, sqlType);
            }
        }
        return procedure;
    }

    public static StoredProcedureBean parseProcedure(String spName, Object param, SessionFactory sessionFactory) throws Exception {
        StoredProcedureBean procedure = new StoredProcedureBean(sessionFactory);
        return parseProcedure(spName, param, procedure);
    }

    public static StoredProcedureBean parseProcedure(String spName, Object param, DataSource dataSource) throws Exception {
        StoredProcedureBean procedure = new StoredProcedureBean();
        procedure.setDataSource(dataSource);
        return parseProcedure(spName, param, procedure);
    }

    private static Object getParamValue(String name, Map<String, Object> paramMap) {
        return paramMap.get(name);
    }
}