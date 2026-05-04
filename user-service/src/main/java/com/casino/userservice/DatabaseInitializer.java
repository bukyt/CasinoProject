package com.casino.userservice;

import javax.sql.DataSource;

import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DatabaseInitializer {

    public static void initialize(String databaseName) {
        String url = "jdbc:postgresql://localhost:5432/";
        String username = "postgres";
        String password = "postgres";

        DataSource dataSource = new DriverManagerDataSource(url, username, password);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        try {
            jdbcTemplate.execute("CREATE DATABASE " + databaseName);
        } catch (Exception e) {
            System.err.println("Database create skipped: " + e.getMessage());
        }
    }
}
