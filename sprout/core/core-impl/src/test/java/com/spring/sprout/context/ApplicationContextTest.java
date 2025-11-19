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
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ApplicationContextTest {

    private ApplicationContext context;
    private final TestClass1 testClass1 = new TestClass1();
    private final TestClass2 testClass2 = new TestClass2();
    private final UniqueClass uniqueClass = new UniqueClass();

    @BeforeEach
    public void setUp() throws Exception {
        context = new ApplicationContext(new Environment(), new ClassScanner());

        context.registerBean("testClass1", testClass1);
        context.registerBean("testClass2", testClass2);
        context.registerBean("uniqueClass", uniqueClass);
    }

    // register Bean test
    @Test
    public void 동일_이름으로_빈_등록시_에러처리() {
        // given, when
        Object newBean = new TestClass1();

        // then
        SpringException e = assertThrows(SpringException.class, () -> {
            context.registerBean("testClass1", newBean);
        });
        assertEquals(ErrorMessage.BEAN_NAME_CONFLICT.getMessage(), e.getMessage());
    }

    // scan Bean test
    @Test
    public void 지정된_패키지에서_Component_스캔하고_빈으로_등록하기() {
        // given
        ApplicationContext scanContext = new ApplicationContext(new Environment(),
            new ClassScanner());
        String scanPackage = "com.spring.sprout.dummy.scan";

        // when
        scanContext.scan(scanPackage);

        // then
        // 1. 기본이름 등록 확인
        Object defaultBean = scanContext.getBean("scanComponent");
        assertNotNull(defaultBean);
        assertTrue(defaultBean instanceof com.spring.sprout.dummy.scan.ScanComponent);

        // 2. 지정이름 확인
        Object namedBean = scanContext.getBean("customBean");
        assertNotNull(namedBean);
        assertTrue(namedBean instanceof com.spring.sprout.dummy.scan.ScanComponentWithName);

        // 3. @Component 없는 클래스는 스캔 대상이 아님
        SpringException exception = assertThrows(SpringException.class, () -> {
            scanContext.getBean("notScanComponent");
        });
        assertEquals(ErrorMessage.NO_BEAN_FOUND_WITH_NAME.getMessage(), exception.getMessage());
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
