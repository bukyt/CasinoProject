package com.casino;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ComplianceClientConfig {

    @Bean
    RestClient complianceRestClient(
        @Value("${services.compliance.base-url}") String complianceBaseUrl
    ) {
        return RestClient.builder()
            .baseUrl(complianceBaseUrl)
            .build();
    }
}