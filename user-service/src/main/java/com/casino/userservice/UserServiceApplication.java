package com.casino.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {
    // http://localhost:8086/swagger-ui/index.html
    public static void main(String[] args) {
        DatabaseInitializer.initialize("userservice_db");
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
