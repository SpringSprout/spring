package com.spring.sprout.io;

import com.spring.sprout.error.ErrorMessage;
import com.spring.sprout.error.SpringException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class ResourcePatternResolver {

    private static final String CLASSPATH_URL_PREFIX = "classpath:";
    private final ClassLoader classLoader;

    public ResourcePatternResolver() {
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public Resource getResource(String location) {
        if (location.startsWith(CLASSPATH_URL_PREFIX)) {
            location = location.substring(CLASSPATH_URL_PREFIX.length());
        }
        return new Resource(location, this.classLoader);
    }

    public Resource[] getResources(String locationPattern) {
        Set<Resource> result = new HashSet<>();

        String path = locationPattern.replace('.', '/');

        try {
            doFindResources(path, result);
        } catch (IOException e) {
            throw new SpringException(ErrorMessage.FILE_NOT_RESOLVED);
        }

        return result.toArray(new Resource[0]);
    }

    private void doFindResources(String path, Set<Resource> result) throws IOException {

        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resourceUrl = resources.nextElement();
            if (resourceUrl.getProtocol().equals("file")) {
                File directory = new File(resourceUrl.getFile());
                findClassResourcesInDirectory(path, directory, result);
            }
        }
    }

    private void findClassResourcesInDirectory(String basePackagePath, File directory,
        Set<Resource> result) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            String fileName = file.getName();
            if (file.isDirectory()) {
                String subPackagePath = basePackagePath + "/" + fileName;
                findClassResourcesInDirectory(subPackagePath, file, result);
            } else if (fileName.endsWith(".class")) {
                String resourcePath = basePackagePath + "/" + fileName;
                result.add(new Resource(resourcePath, this.classLoader));
            }
        }
    }
}
