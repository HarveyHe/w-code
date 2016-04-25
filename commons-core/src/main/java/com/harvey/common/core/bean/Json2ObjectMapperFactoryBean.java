package com.harvey.common.core.bean;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.harvey.common.core.utils.DateUtils;

public class Json2ObjectMapperFactoryBean implements FactoryBean<ObjectMapper>, BeanClassLoaderAware, InitializingBean {
    
    private final Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder().featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private ObjectMapper objectMapper;
    
    private DateFormat dateFormat;

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setCreateXmlMapper(boolean createXmlMapper) {
        this.builder.createXmlMapper(createXmlMapper);
    }

    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setSimpleDateFormat(String format) {
        this.builder.simpleDateFormat(format);
    }

    public void setAnnotationIntrospector(AnnotationIntrospector annotationIntrospector) {
        this.builder.annotationIntrospector(annotationIntrospector);
    }

    public void setPropertyNamingStrategy(PropertyNamingStrategy propertyNamingStrategy) {
        this.builder.propertyNamingStrategy(propertyNamingStrategy);
    }

    public void setSerializationInclusion(JsonInclude.Include serializationInclusion) {
        this.builder.serializationInclusion(serializationInclusion);
    }

    public void setSerializers(JsonSerializer<?>... serializers) {
        this.builder.serializers(serializers);
    }

    public void setSerializersByType(Map<Class<?>, JsonSerializer<?>> serializers) {
        this.builder.serializersByType(serializers);
    }

    public void setDeserializersByType(Map<Class<?>, JsonDeserializer<?>> deserializers) {
        this.builder.deserializersByType(deserializers);
    }

    public void setMixIns(Map<Class<?>, Class<?>> mixIns) {
        this.builder.mixIns(mixIns);
    }

    public void setAutoDetectFields(boolean autoDetectFields) {
        this.builder.autoDetectFields(autoDetectFields);
    }

    public void setAutoDetectGettersSetters(boolean autoDetectGettersSetters) {
        this.builder.autoDetectGettersSetters(autoDetectGettersSetters);
    }

    public void setDefaultViewInclusion(boolean defaultViewInclusion) {
        this.builder.defaultViewInclusion(defaultViewInclusion);
    }

    public void setFailOnUnknownProperties(boolean failOnUnknownProperties) {
        this.builder.failOnUnknownProperties(failOnUnknownProperties);
    }

    public void setFailOnEmptyBeans(boolean failOnEmptyBeans) {
        this.builder.failOnEmptyBeans(failOnEmptyBeans);
    }

    public void setIndentOutput(boolean indentOutput) {
        this.builder.indentOutput(indentOutput);
    }

    /**
     * Specify features to enable.
     * 
     * @see com.fasterxml.jackson.core.JsonParser.Feature
     * @see com.fasterxml.jackson.core.JsonGenerator.Feature
     * @see com.fasterxml.jackson.databind.SerializationFeature
     * @see com.fasterxml.jackson.databind.DeserializationFeature
     * @see com.fasterxml.jackson.databind.MapperFeature
     */
    public void setFeaturesToEnable(Object... featuresToEnable) {
        this.builder.featuresToEnable(featuresToEnable);
    }

    /**
     * Specify features to disable.
     * 
     * @see com.fasterxml.jackson.core.JsonParser.Feature
     * @see com.fasterxml.jackson.core.JsonGenerator.Feature
     * @see com.fasterxml.jackson.databind.SerializationFeature
     * @see com.fasterxml.jackson.databind.DeserializationFeature
     * @see com.fasterxml.jackson.databind.MapperFeature
     */
    public void setFeaturesToDisable(Object... featuresToDisable) {
        this.builder.featuresToDisable(featuresToDisable);
    }

    public void setModules(List<Module> modules) {
        this.builder.modules(modules);
    }

    public void setModulesToInstall(Class<? extends Module>... modules) {
        this.builder.modulesToInstall(modules);
    }

    public void setFindModulesViaServiceLoader(boolean findModules) {
        this.builder.findModulesViaServiceLoader(findModules);
    }

    @Override
    public void setBeanClassLoader(ClassLoader beanClassLoader) {
        this.builder.moduleClassLoader(beanClassLoader);
    }

    @Override
    public void afterPropertiesSet() {
        this.builder.deserializerByType(Date.class,new DateDeseralizer());
        if(this.dateFormat == null){
            this.dateFormat = new SimpleDateFormat(DateUtils.DateFormats.ISO_DATETIME_MILLISECOND_FORMAT);
        }
        this.builder.dateFormat(dateFormat);
        if (this.objectMapper != null) {
            this.builder.configure(this.objectMapper);
        } else {
            this.objectMapper = this.builder.build();
        }
    }
    
    @Override
    public ObjectMapper getObject() {
        return this.objectMapper;
    }

    @Override
    public Class<?> getObjectType() {
        return (this.objectMapper != null ? this.objectMapper.getClass() : null);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
    
    private class DateDeseralizer extends StdDeserializer<Date>{

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public DateDeseralizer(){
            super(Date.class);
        }

        @Override
        public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonToken t = jp.getCurrentToken();
            if(t== JsonToken.VALUE_STRING){
                String value = jp.getText().trim();
                if (value.length() == 0) {
                    return (Date) getEmptyValue();
                }
                try {
                    return DateUtils.parse(value);
                } catch (ParseException e) {
                    throw new IOException(e);
                }
            }
            return super._parseDate(jp, ctxt);
        }

       
        

    }
}
