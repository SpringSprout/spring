package com.spring.sprout.data.support;

import static com.spring.sprout.data.utils.TranslatorToSnake.translateToSnake;

import com.spring.sprout.global.error.ErrorMessage;
import com.spring.sprout.global.error.SpringException;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class EntityMapper<T> {

    private final Class<T> clazz;

    public EntityMapper(Class<T> clazz) {
        // 매핑할 클래스 타입 받기
        this.clazz = clazz;
    }

    public T mapRow(ResultSet rs) throws SQLException {
        try {
            // 1. 빈 객체 생성
            T instance = clazz.getDeclaredConstructor().newInstance();

            // 2. 필드 순회하며 값 주입
            for (Field field : clazz.getDeclaredFields()) {
                String columnName = translateToSnake(field.getName());

                Object value = null;
                try {
                    value = rs.getObject(columnName);
                } catch (SQLException e) {
                    continue; // 컬럼 없으면 패스
                }

                if (value != null) {
                    field.setAccessible(true);
                    field.set(instance, value);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new SpringException(ErrorMessage.ENTITY_MAPPING_FILED);
        }
    }

}
