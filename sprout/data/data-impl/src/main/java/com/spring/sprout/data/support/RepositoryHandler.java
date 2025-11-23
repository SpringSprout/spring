package com.spring.sprout.data.support;

import static com.spring.sprout.data.utils.TranslatorToSnake.translateToSnake;

import com.spring.sprout.JdbcTemplate;
import com.spring.sprout.global.annotation.db.Entity;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * [리포지토리 프록시 호출 핸들러]
 *
 * <p>JDK Dynamic Proxy 메커니즘을 사용하여, 인터페이스로 정의된 리포지토리의 메서드 호출을 가로챕니다.
 * 가로챈 호출 정보를 바탕으로 적절한 SQL을 동적으로 생성하고, {@link JdbcTemplate}을 통해 DB 작업을 수행합니다.</p>
 *
 * <p>이 핸들러 덕분에 개발자는 구현 클래스를 직접 작성하지 않고 인터페이스 정의만으로
 * 기본적인 CRUD(Create, Read, Update, Delete) 기능을 사용할 수 있습니다.</p>
 *
 * <p>작동 원리:</p>
 * <ul>
 * <li><b>메서드 분석:</b> 호출된 메서드 이름(save, findById 등)을 분석하여 실행할 쿼리 유형을 결정합니다.</li>
 * <li><b>SQL 생성:</b> 엔티티 클래스의 메타데이터(필드명, 어노테이션)를 리플렉션으로 읽어 SQL 문을 조립합니다.</li>
 * <li><b>실행 위임:</b> 생성된 SQL과 파라미터를 {@link JdbcTemplate}에게 전달하여 실행합니다.</li>
 * </ul>
 *
 * @see java.lang.reflect.InvocationHandler
 * @see java.lang.reflect.Proxy
 */
public class RepositoryHandler implements InvocationHandler {

    private final JdbcTemplate jdbcTemplate;
    private final Class<?> entityType;

    /**
     * 핸들러 인스턴스를 생성합니다.
     *
     * @param jdbcTemplate DB 쿼리 실행을 담당할 템플릿
     * @param entityType   이 리포지토리가 관리할 도메인 엔티티 클래스 (테이블 매핑 정보 포함)
     */
    public RepositoryHandler(JdbcTemplate jdbcTemplate, Class<?> entityType) {
        this.jdbcTemplate = jdbcTemplate;
        this.entityType = entityType;
    }

    /**
     * 프록시 객체의 메서드가 호출되었을 때 실행되는 진입점입니다. 메서드 이름을 기반으로 적절한 CRUD 로직으로 라우팅(Routing)합니다.
     *
     * @param proxy  프록시 객체 자신
     * @param method 호출된 메서드 객체
     * @param args   메서드에 전달된 인자 목록
     * @return 메서드 실행 결과 (엔티티 또는 리스트)
     * @throws Throwable 실행 중 발생한 예외
     */
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
        // 정의되지 않은 메서드 호출 시 null 반환 (추후 Query Method 파싱 로직 확장 가능)
        return null;
    }

    /**
     * 엔티티 클래스 정보를 바탕으로 매핑된 데이터베이스 테이블 이름을 결정합니다. 1순위: @Entity 어노테이션의 table 속성 2순위: 클래스 이름을 스네이크
     * 케이스로 변환 (Convention over Configuration)
     */
    private String getTableName() {
        if (entityType.isAnnotationPresent(Entity.class)) {
            Entity entity = entityType.getAnnotation(Entity.class);
            if (!entity.table().isEmpty()) {
                return entity.table();
            }
        }
        return translateToSnake(entityType.getSimpleName());
    }

    /**
     * 엔티티 객체의 모든 필드를 읽어 INSERT 쿼리를 동적으로 생성하고 실행합니다. 리플렉션을 사용하여 필드명은 컬럼명으로, 필드값은 바인딩 파라미터로 변환합니다.
     */
    private void save(Object entity) {
        String tableName = getTableName();

        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        StringBuilder values = new StringBuilder("VALUES (");
        List<Object> params = new ArrayList<>();

        Field[] fields = entityType.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            try {
                sql.append(fields[i].getName()); // 주의: 필드명을 그대로 컬럼명으로 사용 중 (스네이크 변환 필요 가능성)
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
        System.out.println(sql.toString());
        jdbcTemplate.execute(sql.toString(), ps -> ps.executeUpdate(), params.toArray());
    }

    /**
     * PK(id)를 기준으로 단건 조회 쿼리를 생성하여 실행합니다. 현재 구현은 PK 컬럼명을 'id'로 가정하고 있습니다.
     */
    private Object findById(Object id) {
        String tableName = getTableName();
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        // 주의: 결과가 없을 경우 getFirst()에서 예외가 발생할 수 있음
        System.out.println(sql);
        return jdbcTemplate.query(sql, entityType, id).getFirst();
    }

    /**
     * 테이블의 모든 데이터를 조회하는 쿼리를 생성하여 실행합니다.
     */
    private Object findAll() {
        String tableName = getTableName();
        String sql = "SELECT * FROM " + tableName;
        System.out.println(sql);
        return jdbcTemplate.query(sql, entityType);
    }
}