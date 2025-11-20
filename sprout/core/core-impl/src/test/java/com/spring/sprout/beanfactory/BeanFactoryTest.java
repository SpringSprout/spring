package com.spring.sprout.beanfactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spring.sprout.dummy.TestClass1;
import com.spring.sprout.dummy.TestClass2;
import com.spring.sprout.dummy.TestInterface;
import com.spring.sprout.dummy.UniqueClass;
import com.spring.sprout.error.ErrorMessage;
import com.spring.sprout.error.SpringException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BeanFactoryTest {

    private BeanFactory beanFactory;

    @BeforeEach
    public void setUp() throws Exception {
        beanFactory = new BeanFactory();

        beanFactory.registerBeanClass(TestClass1.class);
        beanFactory.registerBeanClass(TestClass2.class);
        beanFactory.registerBeanClass(UniqueClass.class);

        beanFactory.preInstantiateSingletons();
    }

    @Test
    public void 이름으로_빈_조회() {
        // given, when
        Object bean = beanFactory.getBean("testClass1");

        // then
        assertNotNull(bean);
        assertTrue(bean instanceof TestClass1);
    }

    @Test
    public void 이름으로_빈_조회_실패() {
        // given, when, then
        assertThrows(SpringException.class,
            () -> beanFactory.getBean("invalidName"),
            ErrorMessage.NO_BEAN_FOUND_WITH_NAME.getMessage());
    }

    @Test
    public void 유일_빈_조회_성공() {
        // given, when
        UniqueClass bean = beanFactory.getBean(UniqueClass.class);

        // then
        assertNotNull(bean);
    }

    @Test
    public void 빈이_없으면_예외() {
        // given, when, then
        assertThrows(SpringException.class,
            () -> beanFactory.getBean(String.class),
            ErrorMessage.NO_BEAN_FOUND_WITH_TYPE.getMessage());
    }

    @Test
    public void _2개이상의_빈() {
        // given, when, then
        assertThrows(SpringException.class,
            () -> beanFactory.getBean(TestInterface.class),
            ErrorMessage.NO_UNIQUE_BEAN_FOUND_WITH_TYPE.getMessage());
    }

    @Test
    public void 인터페이스로_여러_빈_조회_성공() {
        // given, when
        Map<String, TestInterface> beans = beanFactory.getBeansOfType(TestInterface.class);

        // then
        assertEquals(2, beans.size());
        assertTrue(beans.containsKey("testClass1"));
        assertTrue(beans.containsKey("testClass2"));
    }

    @Test
    public void 클래스로_빈_조회_성공() {
        // given, when
        Map<String, UniqueClass> beans = beanFactory.getBeansOfType(UniqueClass.class);

        // then
        assertEquals(1, beans.size());
        assertTrue(beans.containsKey("uniqueClass"));
    }

    @Test
    public void 해당_타입_없으면_빈_map_반환() {
        // given, when
        Map<String, String> beans = beanFactory.getBeansOfType(String.class);

        // then
        assertNotNull(beans);
        assertEquals(0, beans.size());
    }
}
