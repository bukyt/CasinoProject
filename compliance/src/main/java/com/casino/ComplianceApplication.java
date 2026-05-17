package com.casino;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ComplianceApplication {
    public static void main(String[] args) {
        DatabaseInitializer.initialize("compliance_db");
        SpringApplication.run(ComplianceApplication.class, args);
    }

}
