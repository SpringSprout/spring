package com.spring.sprout.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spring.sprout.error.SpringException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class ResourceTest {

    @Test
    public void 리소스_호출() {
        // given, when
        Resource resource = new Resource("test-resource.txt");

        // then
        assertTrue(resource.exists());
    }

    @Test
    public void 존재하지_않는_리소스_호출() {
        // given, when
        Resource resource = new Resource("not-exist-resource.txt");

        // then
        assertFalse(resource.exists());
    }

    @Test
    public void 존재하는_리소스_getInputStream_읽기() {
        // given
        Resource resource = new Resource("test-resource.txt");

        // when
        String content;
        InputStream is = resource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        content = reader.lines().collect(Collectors.joining());

        // then
        assertEquals("hello", content);

    }

    @Test
    public void 존재하지않는_리소스_getInputStream_에러() {
        // given, when
        Resource resource = new Resource("not-exist-resource.txt");

        // then
        assertThrows(SpringException.class, () -> {
            resource.getInputStream();
        });
    }

    @Test
    public void getFile로_file_객체_반환() throws IOException {
        // given
        Resource resource = new Resource("test-resource.txt");

        // when
        File file = resource.getFile();

        // then
        assertNotNull(file);
        assertTrue(file.exists());
        assertEquals("test-resource.txt", file.getName());
    }

    @Test
    void 존재하지_않는_리소스_getFile_예외_발생() {
        // given, then
        Resource resource = new Resource("non-existent-file.txt");

        // then
        assertThrows(SpringException.class, () -> {
            resource.getFile();
        });
    }
}
