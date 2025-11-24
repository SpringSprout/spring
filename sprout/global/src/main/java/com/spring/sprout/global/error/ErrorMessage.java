package com.spring.sprout.global.error;

public enum ErrorMessage {
    // 빈 오류
    NO_BEAN_FOUND_WITH_NAME("해당 이름을 가진 빈은 존재하지 않습니다."),
    NO_BEAN_FOUND_WITH_TYPE("해당 타입을 가진 빈은 존재하지 않습니다."),
    NO_UNIQUE_BEAN_FOUND_WITH_TYPE("해당 타입을 가진 빈은 2개 이상 존재합니다."),

    // 설정 파일 오류
    FILE_NOT_LOADED("설정 파일이 로드되지 않았습니다."),

    // 파일 오류
    FILE_NOT_FOUND("해당 경로에 파일이 없습니다."),
    FILE_NOT_RESOLVED("해당 파일를 로드할 수 없습니다."),

    // 빈 등록 오류
    BEAN_NAME_CONFLICT("동일한 이름의 빈이 이미 있습니다"),

    // 빈 스캔 오류
    BEAN_SCAN_FAILED("빈 스캔에 실패했습니다."),

    // 빈 생성 오류
    BEAN_CREATION_FAILED("빈 생성에 실패했습니다."),

    // 생성자 오류
    NOT_UNIQUE_AUTOWIRED("Autowired 생성자는 하나만 허용됩니다."),

    // 웹 오류
    NOT_FOUND("NOT FOUND"),

    // -- data --
    SQL_EXECUTION_FILED("SQL 실행 중 오류 발생"),

    // 객체 매핑 실패
    ENTITY_MAPPING_FILED("객체 매핑에 실패했습니다."),

    // 엔티티 오류
    ENTITY_TYPE_NOT_FOUND("엔티티 타입을 찾을 수 없습니다");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
