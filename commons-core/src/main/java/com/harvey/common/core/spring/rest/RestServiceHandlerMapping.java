package com.harvey.common.core.spring.rest;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.harvey.common.core.config.Config;
import com.harvey.common.core.config.RunEnv;
import com.harvey.common.core.service.annotation.Rest;
import com.harvey.common.core.spring.listener.RestServiceHandlerMappingListener;
import com.harvey.common.core.utils.ReflectionUtils;

public class RestServiceHandlerMapping extends RequestMappingHandlerMapping {

    private static final Pattern CommonMethodPattern = Pattern.compile("^(init|destroy|equals|hashCode|toString|clone|notify.*|wait|getClass)");
    private static final Pattern ReflectMethodPatter = Pattern.compile("^([sg]et(MetaClass|Property)|.*\\$.*|invokeMethod)");

    private Set<String> excludedPackages = Collections.emptySet();

    private Set<Class<?>> excludedClasses = Collections.emptySet();

    private boolean useSnakeCase = false;

    private String urlPrefix = "";

    private String urlSuffix = "";

    private Class<?> serviceClass;

    private Boolean detectInAncestorContexts = false;

    private RestServiceHandlerMappingListener mappingListener;

    public void setDetectInAncestorContexts(Boolean detectInAncestorContexts) {
        super.setDetectHandlerMethodsInAncestorContexts(detectInAncestorContexts);
        this.detectInAncestorContexts = detectInAncestorContexts;
    }

    public void setUseSnakeCase(boolean useSnakeCase) {
        this.useSnakeCase = useSnakeCase;
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = (urlPrefix != null ? urlPrefix : "");
    }

    public void setUrlSuffix(String urlSuffix) {
        this.urlSuffix = (urlSuffix != null ? urlSuffix : "");
    }

    public void setExcludedPackages(String... excludedPackages) {
        this.excludedPackages = (excludedPackages != null) ? new HashSet<String>(Arrays.asList(excludedPackages)) : new HashSet<String>();
    }

    public void setExcludedClasses(Class<?>... excludedClasses) {
        this.excludedClasses = (excludedClasses != null) ? new HashSet<Class<?>>(Arrays.asList(excludedClasses)) : new HashSet<Class<?>>();
    }

    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    @Override
    protected boolean isHandler(Class<?> beanClass) {
    	
    	if(super.isHandler(beanClass)) {
    		return true;
    	}
    	
        if (this.serviceClass == null) {
            return false;
        }
        if (!this.serviceClass.isAssignableFrom(beanClass)) {
            return false;
        }
        if (this.excludedClasses.contains(beanClass)) {
            return false;
        }
        String beanClassName = beanClass.getName();
        for (String packageName : this.excludedPackages) {
            if (beanClassName.startsWith(packageName)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        if(method.getAnnotation(RequestMapping.class) != null){
            return super.getMappingForMethod(method, handlerType);
        }
        if (!isServiceMethod(method, handlerType)) {
            return null;
        }
        if (Config.getRunEnv().equals(RunEnv.dev)) {
            logger.info("Found Service Method:" + method.toString());
        }
        // create mapping info for method and type level, and combine them
        RequestMappingInfo serviceMapping = createActionMapping(method);
        serviceMapping = (serviceMapping != null ? createControllerMapping(handlerType).combine(serviceMapping) : null);
        if (this.mappingListener != null) {
            Set<String> urls = serviceMapping.getPatternsCondition().getPatterns();
            for (String baseUrl : urls) {
                String url = (StringUtils.isEmpty(this.urlPrefix) ? "" : this.urlPrefix) + baseUrl;
                this.mappingListener.onServiceHandlerMapping(url, handlerType, method);
            }
        }
        return serviceMapping;
    }

    private RequestMappingInfo createActionMapping(Method method) {
        return createConventionalActionMapping(method);
    }

    private RequestMappingInfo createConventionalActionMapping(Method method) {
        return new RequestMappingInfo(createPatternRequestCondition(buildConventionalActions(method)), null, null, null, null, null, getCustomMethodCondition(method));
    }

    private String[] buildConventionalActions(Method method) {
        return new String[] { formatCase(method.getName()) + (urlSuffix == null ? "" : urlSuffix) };
    }

    private RequestMappingInfo createControllerMapping(Class<?> handlerType) {
        return createConventionalControllerMapping(handlerType);
    }

    private RequestMappingInfo createConventionalControllerMapping(Class<?> handlerType) {
        String[] servicePaths = buildConventionalControllerPaths(handlerType);
        PatternsRequestCondition patternsRequestCondition = createPatternRequestCondition(servicePaths);
        RequestCondition<?> requestCondition = getCustomTypeCondition(handlerType);
        return new RequestMappingInfo(patternsRequestCondition, null, null, null, null, null, requestCondition);
    }

    private String[] buildConventionalControllerPaths(Class<?> serviceType) {
        String serviceName = determineServiceName(serviceType);
        return new String[] { '/' + formatCase(serviceName) };
    }

    private PatternsRequestCondition createPatternRequestCondition(String[] patterns) {
        return new PatternsRequestCondition(patterns, this.getUrlPathHelper(), this.getPathMatcher(), useSuffixPatternMatch(), useTrailingSlashMatch());
    }

    private String determineServiceName(Class<?> serviceType) {
        String[] beanNames = this.detectInAncestorContexts ? BeanFactoryUtils.beanNamesForTypeIncludingAncestors(getApplicationContext(), serviceType) : getApplicationContext().getBeanNamesForType(serviceType);
        return beanNames[0];
    }

    private String formatCase(String source) {
        return useSnakeCase ? toSnakeCase(source) : toCamelCase(source);
    }

    private String toSnakeCase(String str) {
        return str.replaceAll("([A-Z])", "-$1").toLowerCase().replaceAll("^-", "");
    }

    private String toCamelCase(String str) {
        if (!StringUtils.hasText(str))
            return str;
        return Character.toLowerCase(str.charAt(0)) + (str.length() > 1 ? str.substring(1) : "");
    }

    private boolean isServiceMethod(Method method, Class<?> handlerType) {
        String name = method.getName();
        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }
        if (CommonMethodPattern.matcher(name).matches()) {
            return false;
        }
        if (ReflectMethodPatter.matcher(name).matches()) {
            return false;
        }
        Rest rest = handlerType.getAnnotation(Rest.class);
        if (rest != null) {
            if ('N' == rest.expose() || '0' == rest.expose()) {
                return false;
            }
        }
        rest = method.getAnnotation(Rest.class);
        if (rest != null) {
            if ('Y' != rest.expose() || '0' == rest.expose()) {
                return false;
            }
        }
        Class<?> serviceType = ReflectionUtils.getServiceInterface(handlerType, serviceClass);
        return ReflectionUtils.hasMethod(serviceType, method);
    }

    public RestServiceHandlerMappingListener getMappingListener() {
        return mappingListener;
    }

    public void setMappingListener(RestServiceHandlerMappingListener mappingListener) {
        this.mappingListener = mappingListener;
    }

    @Override
    public void afterPropertiesSet() {
        if (this.mappingListener == null) {
            try {
                this.mappingListener = BeanFactoryUtils.beanOfTypeIncludingAncestors(getApplicationContext(), RestServiceHandlerMappingListener.class);
            } catch (Exception ex) {

            }
        }
        super.afterPropertiesSet();
    }
}
