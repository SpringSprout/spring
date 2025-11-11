package com.spring.sprout.context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface Resource {

    boolean exists();

    InputStream getInputStream() throws IOException;

    File getFile() throws IOException;

    String getPath();
}
