package com.harvey.common.core.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.StringUtils;

public final class ResourceUtils {
    private final static Log log = LogFactory.getLog(ResourceUtils.class);
    private final static PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private final static MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();

    public static List<Resource> getResources(String pattern) throws IOException {
        return Arrays.asList(resourcePatternResolver.getResources(pattern));
    }

    public static Resource getResource(String pattern) {
        return resourcePatternResolver.getResource(pattern);
    }

    public static List<Resource> getResources(String basePackages, String pattern) {
        if (pattern == null || pattern.length() == 0) {
            pattern = "**.*";
        } else if (!pattern.startsWith("/**/")) {
            pattern = "/**/" + pattern;
        }
        String[] basePackagesArr = StringUtils.commaDelimitedListToStringArray(basePackages);
        for (String basePackage : basePackagesArr) {
            String searchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + org.springframework.util.ClassUtils.convertClassNameToResourcePath(basePackage) + pattern;
            try {
                Resource[] resources = resourcePatternResolver.getResources(searchPath);
                return Arrays.asList(resources);
            } catch (Exception e) {
                log.error(e);
            }
        }
        return Collections.emptyList();
    }

    public static List<Class<?>> getClasses(String basePackages, String pattern) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (pattern == null || pattern.length() == 0) {
            pattern = "*.class";
        } else if (!pattern.endsWith(".class")) {
            pattern = pattern + ".class";
        }
        try {
            List<Resource> resources = getResources(basePackages, pattern);
            for (Resource resource : resources) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                String className = metadataReader.getClassMetadata().getClassName();
                Class<?> clazz = Class.forName(className);
                classes.add(clazz);
            }
        } catch (Exception e) {
            log.error(e);
        }
        return classes;
    }

    public static String getResourcePath(Resource resource) throws Exception {
        if (resource instanceof ContextResource) {
            return '/' + ((ContextResource) resource).getPathWithinContext();
        }
        return resource.getURL().toString();
    }

    public static Boolean isPattern(String pattern) {
        return resourcePatternResolver.getPathMatcher().isPattern(pattern);
    }

    public static Boolean isMatch(String path, String pattern) {
        return resourcePatternResolver.getPathMatcher().match(pattern, path);
    }

    public static Boolean isMatchStart(String path, String pattern) {
        return resourcePatternResolver.getPathMatcher().matchStart(pattern, path);
    }
}
