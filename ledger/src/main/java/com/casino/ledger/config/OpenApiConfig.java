package com.casino.ledger.config;

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
                title = "Ledger Service API",
                version = "v1",
                description = "Read-only API for retrieving ledger entries, player balances, and aggregated ledger stats.",
                contact = @Contact(name = "CasinoProject"),
                license = @License(name = "Internal Use")
        ),
        servers = {
                @Server(url = "http://localhost:8083", description = "Local ledger service")
        },
        tags = {
                @Tag(name = "Ledger", description = "Ledger entry and player ledger queries")
        }
)
public class OpenApiConfig {
}
