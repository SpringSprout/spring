package com.spring.sprout.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResourcePatternResolverTest {

    private ResourcePatternResolver scanner;

    @BeforeEach
    public void setUp() {
        this.scanner = new ResourcePatternResolver();
    }

    @Test
    public void 지정된_패키지_및_하위_패키지의_class_파일_찾기() {
        // given
        String packageName = "com.spring.sprout.dummy";

        // when
        Resource[] resources = scanner.getResources(packageName);

        // then
        assertNotNull(resources);
        Set<String> actualPaths = Stream.of(resources).map(Resource::getPath)
            .collect(Collectors.toSet());
        Set<String> expectedPaths = Set.of(
            "com/spring/sprout/dummy/TestClass1.class",
            "com/spring/sprout/dummy/TestClass2.class",
            "com/spring/sprout/dummy/TestInterface.class",
            "com/spring/sprout/dummy/UniqueClass.class"
        );
        assertEquals(4, resources.length);
        assertEquals(expectedPaths, actualPaths);
    }

    @Test
    public void 존재하지_않는_패키지_스캔_시_빈_배열_반환() {
        // given
        String nonExistingPackage = "com.non.existing.package";

        // when
        Resource[] resources = scanner.getResources(nonExistingPackage);

        // then
        assertNotNull(resources);
        assertEquals(0, resources.length);
    }
}
