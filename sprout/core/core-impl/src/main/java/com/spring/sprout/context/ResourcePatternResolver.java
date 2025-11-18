package com.spring.sprout.context;

import java.io.IOException;

public interface ResourcePatternResolver {

    String CLASSPATH_URL_PREFIX = "classpath:";

    Resource[] getResources(String locationPattern) throws IOException;

    Resource getResource(String location);

    ClassLoader getClassLoader();
}
