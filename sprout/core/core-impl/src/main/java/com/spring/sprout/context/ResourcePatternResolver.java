package com.spring.sprout.context;

public interface ResourcePatternResolver {

    String CLASSPATH_URL_PREFIX = "classpath:";

    Resource[] getResources(String locationPattern);

    Resource getResource(String location);

    ClassLoader getClassLoader();
}
