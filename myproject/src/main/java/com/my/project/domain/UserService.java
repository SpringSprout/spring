package com.my.project.domain;

import com.spring.sprout.data.config.JdbcTemplate;
import com.spring.sprout.global.annotation.Autowired;
import com.spring.sprout.global.annotation.Service;
import com.spring.sprout.global.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 정상 회원 가입 -> 커밋 예정
    @Transactional
    public void join(User user) {
        String sql = "INSERT INTO users (name, age) VALUES (?, ?)";
        jdbcTemplate.execute(sql, ps -> ps.executeUpdate(), user.getName(), user.getAge());
        System.out.println(">> [Service] 회원 가입 완료: " + user.getName());
    }

    // 에러 회원 가입 -> 롤백 예정
    @Transactional
    public void joinError(User user) {
        String sql = "INSERT INTO users (name, age) VALUES (?, ?)";
        jdbcTemplate.execute(sql, ps -> ps.executeUpdate(), user.getName(), user.getAge());
        System.out.println(">> [Service] 데이터 INSERT");
        throw new RuntimeException("오류 발생");
    }
}
