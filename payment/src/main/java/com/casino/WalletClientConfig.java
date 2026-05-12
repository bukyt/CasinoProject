package com.casino;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WalletClientConfig {

    @Bean
    RestClient walletRestClient(
        @Value("${services.wallet.base-url}") String complianceBaseUrl
    ) {
        return RestClient.builder()
            .baseUrl(complianceBaseUrl)
            .build();
    }
}