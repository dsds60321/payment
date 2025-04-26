package dev.gunho.payment.service;

import reactor.core.publisher.Mono;

/**
 * Interface for payment processing services.
 * This interface defines the contract for different payment gateway implementations.
 */
public interface PaymentService {

    /**
     * Creates a payment order.
     *
     * @param amount The amount to be charged
     * @param currency The currency code (e.g., USD, EUR)
     * @param description A description of the payment
     * @return A Mono containing the order ID
     */
    Mono<String> createOrder(Double amount, String currency, String description);

    /**
     * Captures a payment for a previously created order.
     *
     * @param orderId The ID of the order to capture
     * @return A Mono containing true if the capture was successful, false otherwise
     */
    Mono<Boolean> capturePayment(String orderId);

    /**
     * Gets the payment gateway name.
     *
     * @return The name of the payment gateway (e.g., "PayPal", "Stripe")
     */
    String getGatewayName();
}