package com.spring.sprout.context;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class EnvironmentTest {

    @Test
    public void 정상적으로_application_properties_읽어오기() {
        // given
        String configPath = "application.properties";

        // when
        Environment environment = new Environment(configPath);

        // then
        assertEquals("SproutFramework", environment.getProperty("sprout.test.name"));
    }

    @Test
    public void 설정_파일_없을_경우_null_반환() {
        // given
        String configPath = "non-existent-file.properties";

        // when
        Environment environment = null;
        environment = assertDoesNotThrow(() -> new Environment(configPath));

        // then
        assertNull(environment.getProperty("Blah-blah"));
    }

    @Test
    public void 존재하지_않는_키_조회_시_null_반환() {
        // given
        String configPath = "application.properties";
        Environment environment = new Environment(configPath);

        // when
        String value = environment.getProperty("non-existent-key");

        // then
        assertNull(value);
    }
}
