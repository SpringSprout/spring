package com.spring.sprout.data.utils;

/**
 * [네이밍 컨벤션 변환 유틸리티]
 *
 * <p>자바의 표준 네이밍 관례인 '카멜 케이스(CamelCase)'를 데이터베이스의 표준인
 * '스네이크 케이스(Snake_Case)'로 변환하는 기능을 제공합니다.</p>
 *
 * <p>이 클래스는 {@link com.spring.sprout.data.support.EntityMapper} 및
 * {@link com.spring.sprout.data.support.RepositoryHandler}에서 필드명을 테이블 컬럼명으로 자동 매핑할 때 핵심적으로
 * 사용됩니다.</p>
 *
 * <p>설계 특징:</p>
 * <ul>
 * <li><b>Utility Class:</b> 상속이 불가능한 {@code final} 클래스로 설계되었습니다.</li>
 * <li><b>Performance:</b> 빈번한 문자열 조작을 최적화하기 위해 {@link StringBuilder}를 사용합니다.</li>
 * </ul>
 */
public final class TranslatorToSnake {

    /**
     * 카멜 케이스 문자열을 스네이크 케이스로 변환합니다.
     *
     * <p>변환 규칙:</p>
     * <ul>
     * <li>대문자를 만나면 앞에 언더스코어(_)를 추가하고 소문자로 변환합니다.</li>
     * <li>단, 문자열의 첫 번째 글자가 대문자인 경우 언더스코어를 추가하지 않고 소문자로만 변환합니다.</li>
     * </ul>
     *
     * <p>예시:</p>
     * <ul>
     * <li>{@code "userName"} -> {@code "user_name"}</li>
     * <li>{@code "createdAt"} -> {@code "created_at"}</li>
     * <li>{@code "User"} -> {@code "user"} (클래스명을 테이블명으로 변환 시 유용)</li>
     * </ul>
     *
     * @param camelCase 변환할 카멜 케이스 문자열 (null이 아니어야 함)
     * @return 변환된 스네이크 케이스 문자열
     */
    public static String translateToSnake(String camelCase) {
        StringBuilder result = new StringBuilder();
        char[] chars = camelCase.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isUpperCase(c)) {
                // 첫 글자가 대문자인 경우(예: 클래스명)에는 _를 붙이지 않음
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}