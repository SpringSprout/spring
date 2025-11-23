package com.spring.sprout.data.support;

import static com.spring.sprout.data.utils.TranslatorToSnake.translateToSnake;

import com.spring.sprout.global.error.ErrorMessage;
import com.spring.sprout.global.error.SpringException;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * [자동 엔티티 매핑 전략 구현체]
 *
 * <p>JDBC의 {@link ResultSet} 데이터를 자바 객체(Entity)로 자동 변환하는 역할을 수행합니다.
 * 리플렉션(Reflection)을 사용하여 객체를 동적으로 생성하고, 필드 이름을 데이터베이스 컬럼 이름 관례(Snake Case)로 변환하여 값을 주입합니다.</p>
 *
 * <p>매핑 전략:</p>
 * <ul>
 * <li><b>이름 변환:</b> Java 필드명(camelCase) -> DB 컬럼명(snake_case) (예: {@code userId} -> {@code user_id})</li>
 * <li><b>유연한 매핑:</b> 엔티티에는 존재하지만 ResultSet 결과 집합에 없는 컬럼은 무시하고 넘어갑니다. (DTO 매핑 시 유리)</li>
 * <li><b>접근 제어 무시:</b> private 필드에도 값을 주입하기 위해 강제로 접근 권한을 획득합니다.</li>
 * </ul>
 *
 * @param <T> 매핑할 대상 엔티티 클래스 타입
 */
public class EntityMapper<T> {

    private final Class<T> clazz;

    /**
     * 특정 클래스 타입에 대한 매퍼를 생성합니다.
     *
     * @param clazz 데이터를 주입할 대상 클래스 정보
     */
    public EntityMapper(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * ResultSet의 현재 커서(Cursor)가 가리키는 행(Row)을 자바 객체로 변환합니다.
     *
     * <p>작동 원리:</p>
     * <ol>
     * <li>대상 클래스의 기본 생성자를 호출하여 인스턴스를 생성합니다.</li>
     * <li>클래스의 모든 필드를 순회하며 DB 컬럼명으로 변환합니다.</li>
     * <li>ResultSet에서 해당 컬럼 값을 추출하여 필드에 주입합니다.</li>
     * </ol>
     *
     * @param rs 결과 데이터를 담고 있는 ResultSet (커서는 호출자가 관리)
     * @return 데이터가 채워진 엔티티 객체
     * @throws SpringException 객체 생성 실패 또는 매핑 중 치명적인 오류 발생 시
     */
    public T mapRow(ResultSet rs) throws SQLException {
        try {
            // 1. 기본 생성자를 통해 빈 객체 생성 (NoArgsConstructor 필요)
            T instance = clazz.getDeclaredConstructor().newInstance();

            // 2. 필드 순회하며 값 주입 (Reflection)
            for (Field field : clazz.getDeclaredFields()) {
                // 자바 필드명을 DB 컬럼명 포맷(Snake Case)으로 변환
                String columnName = translateToSnake(field.getName());

                Object value = null;
                try {
                    // DB에서 해당 컬럼값 조회 시도
                    value = rs.getObject(columnName);
                } catch (SQLException e) {
                    // ResultSet에 해당 컬럼이 없는 경우(Select 절에 포함 안 된 경우)
                    // 예외를 무시하고 다음 필드로 넘어감 (Lenient Mapping)
                    continue;
                }

                if (value != null) {
                    // private 필드 접근 허용 및 값 주입
                    field.setAccessible(true);
                    field.set(instance, value);
                }
            }
            return instance;
        } catch (Exception e) {
            // 리플렉션 오류 등을 런타임 예외로 포장
            throw new SpringException(ErrorMessage.ENTITY_MAPPING_FILED);
        }
    }
}