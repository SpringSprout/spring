package com.my.project.mvc;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.my.project.domain.User;
import com.spring.sprout.JdbcTemplate;
import com.spring.sprout.bundle.SproutApplication;
import com.spring.sprout.bundle.context.SproutApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserServiceTest {

    SproutApplicationContext context;
    UserService userService;
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        context = SproutApplication.run(com.my.project.MyProjectApplication.class);
        userService = context.getBean(UserService.class);
        jdbcTemplate = context.getBean(JdbcTemplate.class);

        jdbcTemplate.execute("DROP TABLE IF EXISTS user", ps -> ps.execute());

        jdbcTemplate.execute(
            "CREATE TABLE user (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), age INT)",
            ps -> ps.execute());
    }
    
    @Test
    public void 정상_가입_MySQL_연동() {
        // given
        User user = new User(1, "dongju", 25);

        // when
        userService.join(user);

        // then
        User result = userService.find(1);

        assertThat(result.getName()).isEqualTo("dongju");
    }
}
