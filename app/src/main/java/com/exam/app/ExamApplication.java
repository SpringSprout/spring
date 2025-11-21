package com.exam.app;

import com.spring.sprout.context.ApplicationContext;
import com.spring.sprout.SpringApplication;
import com.spring.sprout.data.config.JdbcTemplate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;

public class ExamApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ExamApplication.class);
    }
}
