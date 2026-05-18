package com.casino.bonus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;               
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.web.client.RestTemplate;               

@EnableKafka
@SpringBootApplication
public class BonusApplication {

    public static void main(String[] args) {
        SpringApplication.run(BonusApplication.class, args);
    }

    // ONLY add this if the bonus service needs to call another service via http://service-name
    @Bean
    @LoadBalanced 
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}