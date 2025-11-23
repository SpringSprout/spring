package com.spring.sprout.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spring.sprout.bundle.context.EnvironmentImpl;
import com.spring.sprout.bundle.context.SproutApplicationContext;
import com.spring.sprout.bundle.io.ResourcePatternResolver;
import com.spring.sprout.dummy.TestClass1;
import com.spring.sprout.dummy.TestClass2;
import com.spring.sprout.dummy.UniqueClass;
import com.spring.sprout.dummy.scan.ScanRepository;
import com.spring.sprout.dummy.scan.ScanService;
import com.spring.sprout.global.error.ErrorMessage;
import com.spring.sprout.global.error.SpringException;
import org.junit.jupiter.api.Test;

public class SproutApplicationContextTest {

    private final TestClass1 testClass1 = new TestClass1();
    private final TestClass2 testClass2 = new TestClass2();
    private final UniqueClass uniqueClass = new UniqueClass();
    private SproutApplicationContext context;

    @Test
    public void 패키지_스캔_후_빈_등록_및_조회() {
        // given
        SproutApplicationContext context = new SproutApplicationContext(new EnvironmentImpl(),
            new ResourcePatternResolver());
        String scanPackage = "com.spring.sprout.dummy.scan";

        // when
        context.scan(scanPackage);
        context.refresh();

        // then
        // 1. 기본 이름으로 등록 확인
        Object defaultBean = context.getBean("scanComponent");
        assertNotNull(defaultBean);
        assertTrue(defaultBean.getClass().getName().endsWith("ScanComponent"));

        // 2. 이름으로 지정한 빈 확인
        Object namedBean = context.getBean("customBean");
        assertNotNull(namedBean);
        assertTrue(namedBean.getClass().getName().endsWith("ScanComponentWithName"));
    }

    @Test
    public void 스캔_대상이_아닌_클래스_빈_등록_안됨() {
        // given
        SproutApplicationContext context = new SproutApplicationContext(new EnvironmentImpl(),
            new ResourcePatternResolver());
        String scanPackage = "com.spring.sprout.dummy.scan";

        // when
        context.scan(scanPackage);
        context.refresh();

        // then
        SpringException exception = assertThrows(SpringException.class, () -> {
            context.getBean("notScanComponent");
        });
        assertEquals(ErrorMessage.NO_BEAN_FOUND_WITH_NAME.getMessage(), exception.getMessage());
    }

    @Test
    public void 스캔된_빈들_사이의_의존성_주입_확인() {
        // given
        SproutApplicationContext context = new SproutApplicationContext(new EnvironmentImpl(),
            new ResourcePatternResolver());
        String scanPackage = "com.spring.sprout.dummy.scan";

        // when
        context.scan(scanPackage);
        context.refresh();

        // then
        ScanService service = context.getBean(ScanService.class);
        ScanRepository repository = context.getBean(ScanRepository.class);

        assertNotNull(service);
        assertNotNull(repository);

        assertNotNull(service.getRepository());
        assertSame(repository, service.getRepository());
    }

    @Test
    public void 스캔된_빈은_싱글톤으로_관리된다() {
        // given
        SproutApplicationContext context = new SproutApplicationContext(new EnvironmentImpl(),
            new ResourcePatternResolver());
        String scanPackage = "com.spring.sprout.dummy.scan";

        // when
        context.scan(scanPackage);
        context.refresh();

        // then
        Object bean1 = context.getBean("scanComponent");
        Object bean2 = context.getBean("scanComponent");

        assertNotNull(bean1);
        assertSame(bean1, bean2);
    }
}
