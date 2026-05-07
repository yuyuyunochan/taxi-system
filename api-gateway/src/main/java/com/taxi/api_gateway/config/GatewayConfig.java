package com.taxi.api_gateway.config;

import com.taxi.api_gateway.filter.JwtAuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public GatewayConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("user-auth", r -> r
                        .path("/api/users/auth/**")
                        .uri("http://localhost:8081"))

                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("http://localhost:8081"))

                .route("trip-service", r -> r
                        .path("/api/trips/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("http://localhost:8082"))

                .build();
    }
}