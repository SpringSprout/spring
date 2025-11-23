package com.spring.sprout.annotationtest;

import com.spring.sprout.annotationtest.준비물.component.TestClass1;
import com.spring.sprout.annotationtest.준비물.notcomponent.TestClass2;
import com.spring.sprout.core.beanfactory.BeanFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AnnotationTest {

    private BeanFactory beanFactory = new BeanFactory();

    @Test
    public void 어노테이션을_파고들어_결국_component가_있는지_확인한다() {
        //given
        TestClass1 testClass1 = new TestClass1();

        //when
        boolean isComponent = beanFactory.hasComponentAnnotation(testClass1.getClass());

        //then
        Assertions.assertTrue(isComponent);
    }

    @Test
    public void 어노테이션을_파고들어_결국_component가_없는지_확인한다() {
        //given
        TestClass2 testClass2 = new TestClass2();

        //when
        boolean isComponent = beanFactory.hasComponentAnnotation(testClass2.getClass());

        //then
        Assertions.assertFalse(isComponent);
    }
}
