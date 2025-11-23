package com.spring.sprout.bundle.context;

import com.spring.sprout.bundle.api.Environment;
import com.spring.sprout.global.error.ErrorMessage;
import com.spring.sprout.global.error.SpringException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EnvironmentImpl implements Environment {

    private static final String PROPERTIES_PATH = "application.properties";
    private final Properties properties = new Properties();

    public EnvironmentImpl() {
        loadProperties(PROPERTIES_PATH);
    }

    public EnvironmentImpl(String configPath) {
        loadProperties(configPath);
    }

    private void loadProperties(String fileName) {
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new SpringException(ErrorMessage.FILE_NOT_LOADED);
        }
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
