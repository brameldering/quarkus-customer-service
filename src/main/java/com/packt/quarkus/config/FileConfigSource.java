package com.packt.quarkus.config;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class FileConfigSource implements ConfigSource {
    private static final String CONFIG_FILE = "customconfig.properties";
    private static final String CONFIG_SOURCE_NAME = "ExternalConfigSource";
    private static final int ORDINAL = 900;

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                properties.load(in);
            } else {
                System.err.println("Config file not found on classpath: " + CONFIG_FILE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    @Override
    public Map<String, String> getProperties() {
        Properties props = loadProperties();
        Map<String, String> map = new HashMap<>();
        for (String key : props.stringPropertyNames()) {
            map.put(key, props.getProperty(key));
        }
        return map;
    }

    @Override
    public Set<String> getPropertyNames() {
        return loadProperties().stringPropertyNames();
    }

    @Override
    public String getValue(String name) {
        return loadProperties().getProperty(name);
    }

    @Override
    public String getName() {
        return CONFIG_SOURCE_NAME;
    }

    @Override
    public int getOrdinal() {
        return ORDINAL;
    }
}
