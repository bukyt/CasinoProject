package com.casino.wallet.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Wallet Service API",
                version = "v1",
                description = "API for creating wallets and applying wallet balance debits and credits.",
                contact = @Contact(name = "CasinoProject"),
                license = @License(name = "Internal Use")
        ),
        servers = {
                @Server(url = "http://localhost:8085", description = "Local wallet service")
        },
        tags = {
                @Tag(name = "Wallet", description = "Wallet balance commands and queries")
        }
)
public class OpenApiConfig {
}
