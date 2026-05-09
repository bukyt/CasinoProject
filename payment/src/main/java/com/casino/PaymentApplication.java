package com.casino;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class PaymentApplication {
    public static void main(String[] args) {
        DatabaseInitializer.initialize("payment_db");
        SpringApplication.run(PaymentApplication.class, args);
    }
}
