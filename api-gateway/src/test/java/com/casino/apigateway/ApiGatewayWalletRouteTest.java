package com.casino.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false"
})
class ApiGatewayWalletRouteTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void walletRouteIsConfigured() {
        List<Route> routes = routeLocator.getRoutes().collectList().block();

        assertTrue(routes != null && routes.stream().anyMatch(route ->
                "wallet-service".equals(route.getId())
                        && URI.create("http://localhost:8085").equals(route.getUri())));
    }
}
