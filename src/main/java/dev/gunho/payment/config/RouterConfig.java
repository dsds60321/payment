package dev.gunho.payment.config;

import dev.gunho.payment.handler.payment.PaymentHandler;
import dev.gunho.payment.handler.user.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> route(UserHandler userHandler, PaymentHandler paymentHandler) {
        return RouterFunctions
                .route(POST("/user"), userHandler::createUser)
                .andRoute(POST("/payments/orders"), paymentHandler::createOrder)
                .andRoute(POST("/payments/capture"), paymentHandler::capturePayment);
    }
}
