package com.harvey.common.core.config;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.harvey.common.core.utils.ResourceUtils;

public final class Config {
    private static final Properties config = new Properties();
    private static final String CONFIG_LOCATION = "classpath:/config.properties";
    private static final String RUN_ENV_KEY = "sys.runEnv";
    private static RunEnv runEnv = null;

    static {
        try {
            InputStream is = ResourceUtils.getResource(CONFIG_LOCATION).getInputStream();
            ConfigLoader.loadProperties(config, is);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String get(String key) {
        return config.getProperty(key);
    }

    public static String get(String key, String defVal) {
        return config.getProperty(key, defVal);
    }

    public static RunEnv getRunEnv() {
        if (runEnv == null) {
            String key = get("sys.runEnvKey");
            if (StringUtils.isNotBlank(key)) {
                key = System.getenv(key);
                if (StringUtils.isNotBlank(key)) {
                    try {
                        runEnv = Enum.valueOf(RunEnv.class, key);
                    } catch (Exception ex) {

                    }
                }
            }
            if (runEnv == null) {
                try{
                    runEnv = Enum.valueOf(RunEnv.class, get(RUN_ENV_KEY, "dev"));
                }catch(Exception ex){
                    runEnv = RunEnv.dev;
                }
            }
        }
        return runEnv;
    }

    public static Properties getProperties() {
        Properties properties = new Properties();
        properties.putAll(config);
        return properties;
    }

    public static void mergeProperties(Properties properties) {
        config.putAll(properties);
    }
}
