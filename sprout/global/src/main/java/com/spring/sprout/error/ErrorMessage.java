package com.spring.sprout.error;

public enum ErrorMessage {
    // 빈 오류
    NO_BEAN_FOUND_WITH_NAME("해당 이름을 가진 빈은 존재하지 않습니다."),
    NO_BEAN_FOUND_WITH_TYPE("해당 타입을 가진 빈은 존재하지 않습니다."),
    NO_UNIQUE_BEAN_FOUND_WITH_TYPE("해당 타입을 가진 빈은 2개 이상 존재합니다."),

    // 설정 파일 오류
    FILE_NOT_LOADED("설정 파일이 로드되지 않았습니다."),

    FILE_NOT_FOUND("해당 경로에 파일이 없습니다."),
    FILE_NOT_RESOLVED("해당 파일를 로드할 수 없습니다.");

    private final String message;

    private ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
