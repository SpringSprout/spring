package com.spring.sprout;

public class UserServiceTest {

    ApplicationContext context;
    UserService userService;
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // 1. 컨테이너 실행 (매 테스트마다 독립적인 환경 구성)
        context = SpringApplication.run(ExamApplication.class);
        userService = context.getBean(UserService.class);
        jdbcTemplate = context.getBean(JdbcTemplate.class);

        // 2. 테이블 초기화 (테스트 간 데이터 오염 방지)
        // H2 메모리 DB라도 확실하게 하기 위해 DROP 후 CREATE
        jdbcTemplate.execute("DROP TABLE IF EXISTS users", ps -> ps.execute());
        jdbcTemplate.execute("CREATE TABLE users (name VARCHAR(255), age INT)", ps -> ps.execute());
    }

    @Test
    @DisplayName("정상 회원가입: 트랜잭션 커밋 성공")
    void join_success() {
        // Given (준비)
        User user = new User("Gemini", 25);

        // When (실행)
        userService.join(user);

        // Then (검증)
        // DB를 직접 조회해서 데이터가 1개 있는지 확인
        List<User> users = jdbcTemplate.query("SELECT * FROM users WHERE name = ?", User.class,
            "Gemini");

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getName()).isEqualTo("Gemini");
        assertThat(users.get(0).getAge()).isEqualTo(25);
    }

    @Test
    @DisplayName("예외 발생 시: 트랜잭션 롤백 확인")
    void join_rollback() {
        // Given (준비)
        User user = new User("ErrorMan", 99);

        // When (실행)
        // joinError 메서드는 내부에서 예외를 던짐 -> 이를 감싸서 검증
        assertThatThrownBy(() -> userService.joinError(user))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("예기치 않은 오류");

        // Then (검증)
        // 롤백이 되었다면 DB에 데이터가 없어야 함!
        List<User> users = jdbcTemplate.query("SELECT * FROM users WHERE name = ?", User.class,
            "ErrorMan");

        assertThat(users).isEmpty(); // 0건이어야 성공
    }
}
