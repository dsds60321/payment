package dev.gunho.payment.handler.payment;

import dev.gunho.payment.model.dto.PaymentPayload;
import dev.gunho.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Handler for payment-related HTTP requests.
 */
@Component
@RequiredArgsConstructor
public class PaymentHandler {

    private final PaymentService paymentService;

    /**
     * Creates a payment order.
     *
     * @param request The HTTP request containing the order details
     * @return A Mono containing the server response
     */
    public Mono<ServerResponse> createOrder(ServerRequest request) {
        return request.bodyToMono(PaymentPayload.OrderRequest.class)
                .flatMap(orderRequest -> 
                    paymentService.createOrder(
                            orderRequest.getAmount(),
                            orderRequest.getCurrency(),
                            orderRequest.getDescription()
                    )
                    .map(orderId -> PaymentPayload.OrderResponse.builder()
                            .orderId(orderId)
                            .gatewayName(paymentService.getGatewayName())
                            .amount(orderRequest.getAmount())
                            .currency(orderRequest.getCurrency())
                            .description(orderRequest.getDescription())
                            .userId(orderRequest.getUserId())
                            .build()
                    )
                )
                .flatMap(orderResponse -> 
                    ServerResponse.created(URI.create("/payments/orders/" + orderResponse.getOrderId()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(orderResponse)
                );
    }

    /**
     * Captures a payment for a previously created order.
     *
     * @param request The HTTP request containing the capture details
     * @return A Mono containing the server response
     */
    public Mono<ServerResponse> capturePayment(ServerRequest request) {
        return request.bodyToMono(PaymentPayload.CaptureRequest.class)
                .flatMap(captureRequest -> 
                    paymentService.capturePayment(captureRequest.getOrderId())
                    .map(success -> PaymentPayload.CaptureResponse.builder()
                            .orderId(captureRequest.getOrderId())
                            .gatewayName(paymentService.getGatewayName())
                            .success(success)
                            .userId(captureRequest.getUserId())
                            .build()
                    )
                )
                .flatMap(captureResponse -> 
                    ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(captureResponse)
                );
    }
}