package com.casino.profileservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ProfileServiceApplication {
    // http://localhost:8086/swagger-ui/index.html
    public static void main(String[] args) {
        DatabaseInitializer.initialize("profileservice_db");
        SpringApplication.run(ProfileServiceApplication.class, args);
    }
}
