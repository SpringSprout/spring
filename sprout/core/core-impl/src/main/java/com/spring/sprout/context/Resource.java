package com.spring.sprout.context;

import com.spring.sprout.error.ErrorMessage;
import com.spring.sprout.error.SpringException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Resource {

    private final String path;
    private final ClassLoader classLoader;

    public Resource(String path) {
        this.path = path;
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    public Resource(String path, ClassLoader classLoader) {
        this.path = path;
        this.classLoader = classLoader;
    }

    public String getPath() {
        return path;
    }

    public boolean exists() {
        return classLoader.getResource(this.path) != null;
    }

    public InputStream getInputStream() {
        InputStream is = classLoader.getResourceAsStream(this.path);
        if (is == null) {
            throw new SpringException(ErrorMessage.FILE_NOT_FOUND);
        }
        return is;
    }

    public File getFile() {
        URL url = classLoader.getResource(this.path);
        if (url == null) {
            throw new SpringException(ErrorMessage.FILE_NOT_FOUND);
        }
        if (!"file".equals(url.getProtocol())) {
            throw new SpringException(ErrorMessage.FILE_NOT_RESOLVED);

        }
        try {
            return new File(url.toURI());
        } catch (java.net.URISyntaxException e) {
            return new File(url.getFile());
        }
    }

}
