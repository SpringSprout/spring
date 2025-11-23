package com.spring.sprout.data.support;

import static com.spring.sprout.data.utils.TranslatorToSnake.translateToSnake;

import com.spring.sprout.JdbcTemplate;
import com.spring.sprout.global.annotation.db.Entity;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RepositoryHandler implements InvocationHandler {

    private final JdbcTemplate jdbcTemplate;
    private final Class<?> entityType;

    public RepositoryHandler(JdbcTemplate jdbcTemplate, Class<?> entityType) {
        this.jdbcTemplate = jdbcTemplate;
        this.entityType = entityType;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (methodName.equals("save")) {
            save(args[0]);
            return null;
        } else if (methodName.equals("findById")) {
            return findById(args[0]);
        } else if (methodName.equals("findAll")) {
            return findAll();
        }
        return null;
    }

    // table 이름
    private String getTableName() {
        // 1. @Entity 어노테이션 확인
        if (entityType.isAnnotationPresent(Entity.class)) {
            Entity entity = entityType.getAnnotation(Entity.class);
            if (!entity.table().isEmpty()) {
                return entity.table();
            }
        }
        return translateToSnake(entityType.getSimpleName());
    }

    private void save(Object entity) {
        String tableName = getTableName();

        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        StringBuilder values = new StringBuilder("VALUES (");
        List<Object> params = new ArrayList<>();

        Field[] fields = entityType.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            try {
                sql.append(fields[i].getName());
                values.append("?");
                params.add(fields[i].get(entity));

                if (i < fields.length - 1) {
                    sql.append(", ");
                    values.append(", ");
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        sql.append(") ").append(values).append(")");
        jdbcTemplate.execute(sql.toString(), ps -> ps.executeUpdate(), params.toArray());
    }

    private Object findById(Object id) {
        String tableName = getTableName();
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        return jdbcTemplate.query(sql, entityType, id).getFirst();
    }

    private Object findAll() {
        String tableName = getTableName();
        String sql = "SELECT * FROM " + tableName;
        return jdbcTemplate.query(sql, entityType);
    }
}
