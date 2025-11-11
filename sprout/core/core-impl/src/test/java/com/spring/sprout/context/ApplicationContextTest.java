package com.spring.sprout.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spring.sprout.dummy.TestClass1;
import com.spring.sprout.dummy.TestClass2;
import com.spring.sprout.dummy.TestInterface;
import com.spring.sprout.dummy.UniqueClass;
import com.spring.sprout.error.ErrorMessage;
import com.spring.sprout.error.SpringException;
import java.lang.reflect.Field;
import java.util.Map;
import javax.swing.Spring;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ApplicationContextTest {

    private ApplicationContext context;
    private final TestClass1 testClass1 = new TestClass1();
    private final TestClass2 testClass2 = new TestClass2();
    private final UniqueClass uniqueClass = new UniqueClass();

    @BeforeEach
    public void setUp() throws Exception {
        context = new ApplicationContext();
        Field registryField = ApplicationContext.class.getDeclaredField("beanRegistry");
        registryField.setAccessible(true);
        Map<String, Object> beanRegistry = (Map<String, Object>) registryField.get(context);

        beanRegistry.put("testClass1", testClass1);
        beanRegistry.put("testClass2", testClass2);
        beanRegistry.put("uniqueClass", uniqueClass);
    }

    // getBean(String name) test

    @Test
    public void 이름으로_빈_조회() {
        // given, when
        Object bean = context.getBean("testClass1");

        // then
        assertSame(testClass1, bean);
    }

    @Test
    public void 이름으로_빈_조회_실패() {
        // given
        String notExistBeanName = "testClass3";

        // when, then
        SpringException exception = assertThrows(SpringException.class,
            () -> context.getBean(notExistBeanName));
        assertEquals(ErrorMessage.NO_BEAN_FOUND_WITH_NAME.getMessage(), exception.getMessage());
    }

    // getBean(Class<T> requiredType) test

    @Test
    public void 유일_빈_조회_성공() {
        // given, when
        UniqueClass bean = context.getBean(UniqueClass.class);

        // then
        assertNotNull(bean);
        assertSame(uniqueClass, bean);
    }

    @Test
    public void 빈이_없으면_예외() {
        // given, when, then
        SpringException exception = assertThrows(SpringException.class, () -> {
            context.getBean(String.class);
        });

        assertEquals(ErrorMessage.NO_BEAN_FOUND_WITH_TYPE.getMessage(), exception.getMessage());
    }

    @Test
    public void _2개이상의_빈() {
        // given, when, then
        SpringException exception = assertThrows(SpringException.class, () -> {
            context.getBean(TestInterface.class);
        });

        assertEquals(ErrorMessage.NO_UNIQUE_BEAN_FOUND_WITH_TYPE.getMessage(),
            exception.getMessage());
    }

    // givenBeansOfType(Class<T> type) test

    @Test
    public void 인터페이스로_여러_빈_조회_성공() {
        // given, when
        Map<String, TestInterface> beans = context.getBeansOfType(TestInterface.class);

        // then
        assertEquals(2, beans.size());
        assertTrue(beans.containsKey("testClass1"));
        assertTrue(beans.containsKey("testClass2"));
    }

    @Test
    public void 클래스로_빈_조회_성공() {
        // given, when
        Map<String, UniqueClass> beans = context.getBeansOfType(UniqueClass.class);

        // then
        assertEquals(1, beans.size());
        assertTrue(beans.containsKey("uniqueClass"));
    }

    @Test
    public void 해당_타입_없으면_빈_map_반환() {
        // given, when
        Map<String, String> beans = context.getBeansOfType(String.class);

        // then
        assertNotNull(beans);
        assertEquals(0, beans.size());
    }
}
