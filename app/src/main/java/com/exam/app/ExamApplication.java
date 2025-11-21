package com.exam.app;

import com.exam.app.domain.Product;
import com.spring.sprout.SpringApplication;
import com.spring.sprout.context.ApplicationContext;
import com.spring.sprout.data.config.JdbcTemplate;
import java.util.List;

public class ExamApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ExamApplication.class);
    }
}
