package com.my.project.mvc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import com.my.project.MyProjectApplication;
import com.my.project.domain.User;
import com.spring.sprout.JdbcTemplate;
import com.spring.sprout.bundle.SproutApplication;
import com.spring.sprout.bundle.context.SproutApplicationContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserServiceTest {

    SproutApplicationContext context;
    UserService userService;
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        // 1. 컨테이너 실행
        context = SproutApplication.run(MyProjectApplication.class);
        userService = context.getBean(UserService.class);
        jdbcTemplate = context.getBean(JdbcTemplate.class);

        // 2. 테이블 초기화
        jdbcTemplate.execute("DROP TABLE IF EXISTS users", ps -> ps.execute());
        jdbcTemplate.execute("CREATE TABLE users (name VARCHAR(255), age INT)", ps -> ps.execute());
    }

    @Test
    public void 정상_가입() {
        // given
        User user = new User("Gemini", 25);

        // when
        userService.join(user);

        // then
        List<User> users = jdbcTemplate.query("SELECT * FROM users WHERE name = ?", User.class,
            "Gemini");

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getName()).isEqualTo("Gemini");
        assertThat(users.get(0).getAge()).isEqualTo(25);
    }

    @Test
    public void 에러_발생_가입() {
        // given
        User user = new User("ErrorMan", 99);

        // when
        assertThatThrownBy(() -> userService.joinError(user))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("오류 발생");

        // then
        List<User> users = jdbcTemplate.query("SELECT * FROM users WHERE name = ?", User.class,
            "ErrorMan");

        assertThat(users).isEmpty(); // 0건
    }
}
