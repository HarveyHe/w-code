package com.harvey.common.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harvey.common.core.bean.Json2ObjectMapperFactoryBean;
import com.harvey.common.core.context.Context;

public final class JSON {
    public static final JSON Instance = new JSON();
    private static final ObjectMapper objMapper = buildJsonObjectMapper();

    public static ObjectMapper buildJsonObjectMapper() {
        Json2ObjectMapperFactoryBean json2ObjBean = null;
        if (Context.getContext() != null) {
            try {
                json2ObjBean = Context.getContext().getBean(Json2ObjectMapperFactoryBean.class);
            } catch (Exception ex) {

            }
        }
        if (json2ObjBean == null) {
            json2ObjBean = new Json2ObjectMapperFactoryBean();
            json2ObjBean.afterPropertiesSet();
        }
        return json2ObjBean.getObject();
    }

    public String stringify(Object obj) {
        return serialize(obj);
    }

    public static ObjectMapper getObjectMapper() {
        return objMapper;
    }

    public static <T> T deSerialize(Class<T> clazz, String json) throws JsonParseException, JsonMappingException, IOException {
        return objMapper.readValue(json, clazz);
    }

    public static <T> T deSerialize(Class<T> clazz, Reader reader) throws JsonParseException, JsonMappingException, IOException {
        return objMapper.readValue(reader, clazz);
    }

    public static <T> T deSerialize(Class<T> clazz, InputStream stream) throws JsonParseException, JsonMappingException, IOException {
        return objMapper.readValue(stream, clazz);
    }

    public static <T> T deSerialize(String json, Class<T> clazz, Class<?>... classes) throws JsonParseException, JsonMappingException, IOException {
        JavaType javaType = getCollectionType(clazz, classes);
        return objMapper.readValue(json, javaType);
    }

    public static String serialize(Object obj) {
        try {
            Writer writer = new StringWriter();
            serialize(writer, obj);
            return writer.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void serialize(Writer writer, Object obj) throws IOException {
        JsonGenerator generator = createGenerator(writer);
        objMapper.writeValue(generator, obj);
    }

    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return objMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    private static JsonGenerator createGenerator(Writer writer) throws IOException {
        return objMapper.getFactory().createGenerator(writer);
    }
}
