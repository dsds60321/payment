package dev.gunho.global.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Web filter for payment-related requests.
 * This filter logs all incoming requests and could be extended to implement
 * authentication, authorization, rate limiting, etc.
 */
@Slf4j
@Component
public class PaymentWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        log.info("Request: {} {}", method, path);

        // Continue the filter chain
        return chain.filter(exchange)
                .doOnSuccess(v -> log.info("Response: {} {} - Success", method, path))
                .doOnError(e -> log.error("Response: {} {} - Error: {}", method, path, e.getMessage()));
    }
}
