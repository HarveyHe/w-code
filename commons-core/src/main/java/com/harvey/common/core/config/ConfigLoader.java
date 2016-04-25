package com.harvey.common.core.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.harvey.common.core.utils.DetectEncoding;
import com.harvey.common.core.utils.ResourceUtils;

public class ConfigLoader {
    private static final String ConfigLocationSuffix = "-configlocation";
    private static final String PropertiesSuffix = ".properties";
    
    private Map<String, Resource> propertiesMap = new TreeMap<String, Resource>();
    private final List<String> appContextList = new ArrayList<String>();
    private final List<String> servletContextList = new ArrayList<String>();
    private final List<String> propRunModesSuffix = new ArrayList<String>();
    private final List<String> xmlRunModesSuffix = new ArrayList<String>();
    private final String propRunModeSuffix;
    private final String xmlRunModeSuffix;

    public static ConfigLoader loadConfig(ServletContext context){
        ConfigLoader loader = (ConfigLoader) context.getAttribute(ConfigLoader.class.getName());
        if(loader == null){
            loader = new ConfigLoader(context);
            context.setAttribute(ConfigLoader.class.getName(),loader);
        }
        return loader;
    }
    
    public ConfigLoader(ServletContext context){
        this(getServletInitParameters(context));
    }
    
    public ConfigLoader(Map<String,String> configMap){
        RunEnv mode = Config.getRunEnv();
        propRunModeSuffix = "-" + mode.toString() + PropertiesSuffix;
        xmlRunModeSuffix = "-" + mode.toString() + ".xml";
        for (RunEnv runMode : RunEnv.values()) {
            if (!runMode.equals(mode)) {
                propRunModesSuffix.add("-" + runMode.toString() + PropertiesSuffix);
                xmlRunModesSuffix.add("-" + runMode.toString() + ".xml");
            }
        }
        loadConfig(configMap);
    }

    public List<String> getAppContextList() {
        return appContextList;
    }

    public List<String> getServletContextList() {
        return servletContextList;
    }

    public List<String> getServletContextList(String pattern) {
        List<String> list = new ArrayList<String>();
        for(String path : servletContextList){
            if(ResourceUtils.isMatch(path, pattern)){
                list.add(path);
            }
        }
        return list;
    }

    private void loadConfig(Map<String,String> configMap) {
        for(Entry<String,String> entry : configMap.entrySet()){
            if (!StringUtils.endsWithIgnoreCase(entry.getKey(), ConfigLocationSuffix)) {
                continue;
            }
            String[] locations = StringUtils.tokenizeToStringArray(entry.getValue(), ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            for (String location : locations) {
                try {
                    if (ResourceUtils.isPattern(location)) {
                        processLocationPattern(location);
                    } else {
                        processLocation(location, null);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        processLoadedXml(appContextList);
        processLoadedXml(servletContextList);
        try {
            processLoadedProperties();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processLoadedProperties() throws Exception {
        Properties logConfig = new Properties();
        for (Entry<String, Resource> entry : propertiesMap.entrySet()) {
            InputStream is = entry.getValue().getInputStream();
            if (((String) entry.getKey()).contains("log4j")) {
                //logConfig.load(is);
                loadProperties(logConfig, is);
            }else{
                Properties properties = new Properties();
                loadProperties(properties, is);
                Config.mergeProperties(properties);
            }

        }
        configLog4j(logConfig);
    }

    private static void configLog4j(Properties logConfig) {
        try {
            Class<?> clazz = Class.forName("org.apache.log4j.PropertyConfigurator");
            clazz.getDeclaredMethod("configure", Properties.class).invoke(null, logConfig);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void processLoadedXml(List<String> target) {
        List<String> tempList = new ArrayList<String>(target);
        for (String path : tempList) {
            if (StringUtils.endsWithIgnoreCase(path, xmlRunModeSuffix)) {
                String path1 = path.replace(xmlRunModeSuffix, ".xml");
                target.remove(path1);
            }
        }
    }

    private void processLocationPattern(String location) throws Exception {
        List<Resource> resources = ResourceUtils.getResources(location);
        for (Resource resource : resources) {
            location = ResourceUtils.getResourcePath(resource);
            processLocation(location, resource);
        }
    }

    private void processLocation(String location, Resource origResource) throws Exception {

        if (isXmlLocation(location)) {
            List<String> target = null;
            if (isAppContextLocation(location)) {
                target = appContextList;
            } else if (isServletContextLocation(location,"*")) {
                target = servletContextList;
            } 
            if (target != null) {
                processXmlConfigs(target, location);
            }
        } else if (isPropertiesLocation(location)) {
            if (ResourceUtils.isPattern(location)) {
                List<Resource> resources = ResourceUtils.getResources(location);
                for (Resource resource : resources) {
                    processPropertiesConfigs(resource);
                }
            } else if (origResource != null) {
                processPropertiesConfigs(origResource);
            } else {
                Resource resource = ResourceUtils.getResource(location);
                processPropertiesConfigs(resource);
            }
        }
    }

    private static Boolean isAppContextLocation(String location) {
        location = location.toLowerCase();
        return ResourceUtils.isMatch(location, "*applicationcontext*.xml") || ResourceUtils.isMatch(location, "**/*applicationcontext*.xml") || ResourceUtils.isMatch(location, "**.*applicationcontext*.xml");
    }

    private static Boolean isServletContextLocation(String location, String flag) {
        location = location.toLowerCase();
        return ResourceUtils.isMatch(location, "*servletcontext-" + flag + "*.xml") || ResourceUtils.isMatch(location, "**/*servletcontext-" + flag + "*.xml") || ResourceUtils.isMatch(location, "**.*servletcontext-" + flag + "*.xml");
    }

    private static Boolean isPropertiesLocation(String location) {
        return StringUtils.endsWithIgnoreCase(location, PropertiesSuffix);
    }

    private static Boolean isXmlLocation(String location) {
        return StringUtils.endsWithIgnoreCase(location, ".xml");
    }

    private void processXmlConfigs(List<String> target, String location) {
        if (StringUtils.endsWithIgnoreCase(location, xmlRunModeSuffix)) {
            target.add(location);
        } else {
            for (String runMode : xmlRunModesSuffix) {
                if (StringUtils.endsWithIgnoreCase(location, runMode)) {
                    return;
                }
            }
            target.add(location);
        }
    }

    private void processPropertiesConfigs(Resource resource) throws Exception {
        String path = resource.getURI().toString();
        String fileName = resource.getFilename();
        //trim file ext name
        path = path.substring(0, path.length() - PropertiesSuffix.length());
        if (StringUtils.endsWithIgnoreCase(fileName, propRunModeSuffix)) {
            propertiesMap.put(path, resource);
        } else {
            for (String runMode : propRunModesSuffix) {
                if (StringUtils.endsWithIgnoreCase(fileName, runMode)) {
                    return;
                }
            }
            propertiesMap.put(path, resource);
        }
    }

    public Properties loadProperties(InputStream is) throws IOException {
        Properties prop = new Properties();
        loadProperties(prop, is);
        return prop;
    }

    public static void loadProperties(Properties prop, InputStream is) throws IOException {
        byte[] bytes = IOUtils.toByteArray(is);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        String csName = DetectEncoding.Instance.detect(bytes);
        prop.load(new InputStreamReader(bis, csName));
        is.close();
    }
    
    static Map<String,String> getServletInitParameters(ServletContext context){
        Map<String,String> configMap = new LinkedHashMap<String,String>();
        Enumeration<String> paramNames = context.getInitParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if (!StringUtils.endsWithIgnoreCase(paramName, ConfigLocationSuffix)) {
                continue;
            }
            configMap.put(paramName, context.getInitParameter(paramName));
        }
        return configMap;
    }
}
