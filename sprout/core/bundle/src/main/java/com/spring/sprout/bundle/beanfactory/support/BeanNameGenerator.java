package com.spring.sprout.bundle.beanfactory.support;

import com.spring.sprout.global.annotation.Component;

/**
 * [빈 이름 생성 전략 구현체]
 *
 * <p>등록된 빈 클래스에 대해 컨테이너 내에서 식별 가능한 유일한 이름을 부여하는 전략을 정의합니다.
 * 어노테이션에 명시된 이름을 최우선으로 사용하며, 명시된 이름이 없을 경우 표준 Java 코딩 컨벤션(CamelCase)에 따라 기본 이름을 생성합니다.</p>
 *
 * <p>주요 전략:</p>
 * <ul>
 * <li>{@link Component} 어노테이션의 value 속성이 존재할 경우 이를 사용</li>
 * <li>그 외의 경우, 클래스 이름(SimpleName)의 첫 글자를 소문자로 변환하여 사용 (예: UserServiceImpl -> userServiceImpl)</li>
 * <li>단, 클래스 이름의 첫 번째와 두 번째 글자가 모두 대문자인 경우 원본 이름을 유지 (JavaBeans 규약 준수)</li>
 * </ul>
 *
 * @see com.spring.sprout.global.annotation.Component
 */
public class BeanNameGenerator {

    /**
     * 주어진 클래스에 대한 빈 이름을 결정합니다.
     *
     * <p>{@link Component} 어노테이션을 검사하여 사용자 정의 이름이 있는지 확인하고,
     * 없을 경우 기본 네이밍 규칙(Decapitalize)을 적용합니다.</p>
     *
     * @param clazz 이름을 생성할 대상 클래스 정보
     * @return 컨테이너에 등록될 빈의 이름 (결코 null을 반환하지 않음)
     */
    public String determineBeanName(Class<?> clazz) {
        Component component = clazz.getAnnotation(Component.class);
        if (component != null) {
            String value = component.value();
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }

        String className = clazz.getSimpleName();
        return decapitalize(className);
    }

    /**
     * 문자열의 첫 글자를 소문자로 변환하는 유틸리티 메서드입니다.
     *
     * <p>표준 JavaBeans의 {@code Introspector.decapitalize} 규약을 따릅니다.
     * 일반적으로 첫 글자를 소문자로 변환하지만, 첫 글자와 두 번째 글자가 모두 대문자인 경우 (예: "URLBased...", "XMLParser") 원본 문자열을 그대로
     * 반환합니다.</p>
     *
     * @param name 변환할 원본 문자열 (일반적으로 클래스의 SimpleName)
     * @return 변환된 문자열
     */
    private String decapitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        // URL, XML 등 연속된 대문자로 시작하는 경우 소문자 변환을 하지 않음 (JavaBeans 규약)
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) &&
            Character.isUpperCase(name.charAt(0))) {
            return name;
        }
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}