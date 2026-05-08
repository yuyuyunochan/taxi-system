package com.taxi.api_gateway.filter;

import com.taxi.api_gateway.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    private record OpenEndpoint(HttpMethod method, String path, MatchType matchType) {}

    private enum MatchType { EXACT, PREFIX }

    private static final List<OpenEndpoint> OPEN_API_ENDPOINTS = List.of(
            new OpenEndpoint(HttpMethod.POST, "/api/users/auth/login",    MatchType.EXACT),
            new OpenEndpoint(HttpMethod.POST, "/api/users/auth/register", MatchType.EXACT),
//            new OpenEndpoint(HttpMethod.POST, "/api/users/passengers",    MatchType.EXACT),
//            new OpenEndpoint(HttpMethod.POST, "/api/users/drivers",       MatchType.EXACT),

            new OpenEndpoint(null, "/v3/api-docs",        MatchType.PREFIX),
            new OpenEndpoint(null, "/user/v3/api-docs",   MatchType.PREFIX),
            new OpenEndpoint(null, "/trip/v3/api-docs",   MatchType.PREFIX),
            new OpenEndpoint(null, "/swagger-ui",         MatchType.PREFIX),
            new OpenEndpoint(null, "/swagger-resources",  MatchType.PREFIX),
            new OpenEndpoint(null, "/webjars",            MatchType.PREFIX)
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        if (isOpenEndpoint(path, method)) {
            log.debug("Open endpoint, skipping JWT check: {} {}", method, path);
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {} {}", method, path);
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid JWT token for path: {} {}", method, path);
                return unauthorized(exchange, "Invalid JWT token");
            }

            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            log.info("JWT OK - UserId: {}, Role: {}, {} {}", userId, role, method, path);

            final String userIdStr = userId != null ? userId.toString() : "0";
            final String roleStr = role != null ? role : "USER";

            ServerHttpRequest originalRequest = exchange.getRequest();

            HttpHeaders newHeaders = new HttpHeaders();
            newHeaders.putAll(originalRequest.getHeaders());
            newHeaders.set("X-User-Id", userIdStr);
            newHeaders.set("X-User-Role", roleStr);

            ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(originalRequest) {
                @Override
                public HttpHeaders getHeaders() {
                    return newHeaders;
                }
            };

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("JWT parse error: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return unauthorized(exchange, "JWT parse error: " + e.getClass().getSimpleName());
        }
    }

    private boolean isOpenEndpoint(String path, HttpMethod method) {
        return OPEN_API_ENDPOINTS.stream().anyMatch(endpoint -> {
            boolean methodMatches = endpoint.method() == null || endpoint.method().equals(method);
            if (!methodMatches) return false;
            return switch (endpoint.matchType()) {
                case EXACT  -> path.equals(endpoint.path());
                case PREFIX -> path.startsWith(endpoint.path());
            };
        });
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}