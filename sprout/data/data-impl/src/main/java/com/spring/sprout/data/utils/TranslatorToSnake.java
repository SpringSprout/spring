package com.spring.sprout.data.utils;

public final class TranslatorToSnake {

    public static String translateToSnake(String camelCase) {
        StringBuilder result = new StringBuilder();
        char[] chars = camelCase.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isUpperCase(c)) {
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
