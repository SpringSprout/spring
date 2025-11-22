package com.spring.sprout.core.io;

import com.spring.sprout.global.error.ErrorMessage;
import com.spring.sprout.global.error.SpringException;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
            String protocol = resourceUrl.getProtocol();
            if ("file".equals(protocol)) {
                File directory = new File(resourceUrl.getFile());
                findClassResourcesInDirectory(path, directory, result);
            } else if ("jar".equals(protocol)) {
                findClassResourcesInJar(path, resourceUrl, result);
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

    private void findClassResourcesInJar(String path, URL resourceUrl, Set<Resource> result)
        throws IOException {
        URLConnection con = resourceUrl.openConnection();

        if (!(con instanceof JarURLConnection)) {
            return;
        }

        JarURLConnection jarCon = (JarURLConnection) con;
        jarCon.setUseCaches(false);

        try (JarFile jarFile = jarCon.getJarFile()) {
            Collections.list(jarFile.entries()).stream()
                .map(JarEntry::getName)
                .filter(name -> name.startsWith(path) && name.endsWith(".class"))
                .map(name -> new Resource(name, this.classLoader))
                .forEach(result::add);
        }
    }
}
