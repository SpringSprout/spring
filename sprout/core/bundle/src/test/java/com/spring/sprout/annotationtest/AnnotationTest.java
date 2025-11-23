package com.spring.sprout.annotationtest;

import com.spring.sprout.annotationtest.준비물.component.Component;
import com.spring.sprout.annotationtest.준비물.component.TestClass1;
import com.spring.sprout.annotationtest.준비물.notcomponent.TestClass2;
import com.spring.sprout.bundle.beanfactory.BeanFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AnnotationTest {

    private BeanFactory beanFactory = new BeanFactory();

    @Test
    public void 어노테이션을_파고들어_결국_component가_있는지_확인한다() {
        //given
        TestClass1 testClass1 = new TestClass1();

        //when
        boolean isComponent1 = beanFactory.hasComponentAnnotation(testClass1.getClass());
        boolean isComponent2 = beanFactory.hasComponentAnnotation(testClass1.getClass());

        //then
        Assertions.assertTrue(isComponent1);
        Assertions.assertTrue(isComponent2);
    }

    @Test
    public void 어노테이션을_파고들어_결국_component가_없는지_확인한다() {
        //given
        TestClass2 testClass2 = new TestClass2();

        //when
        boolean isComponent1 = beanFactory.hasComponentAnnotation(testClass2.getClass());
        boolean isComponent2 = beanFactory.hasComponentAnnotation(testClass2.getClass());

        //then
        Assertions.assertFalse(isComponent1);
        Assertions.assertFalse(isComponent2);
    }

    @Test
    public void isAnnotationPresent로는_메타어노테이션을_볼수없다() {
        //given
        TestClass1 testClass1 = new TestClass1();

        //when
        boolean isComponent = testClass1.getClass().isAnnotationPresent(Component.class);

        //then
        Assertions.assertFalse(isComponent);
    }
}
