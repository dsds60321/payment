package dev.gunho.payment.model.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Data transfer objects for payment operations.
 */
public class PaymentPayload {

    /**
     * Request payload for creating a payment order.
     */
    @Getter
    @Builder
    public static class OrderRequest {
        private Double amount;
        private String currency;
        private String description;
        private String userId;
    }

    /**
     * Response payload for a created payment order.
     */
    @Getter
    @Builder
    public static class OrderResponse {
        private String orderId;
        private String gatewayName;
        private Double amount;
        private String currency;
        private String description;
        private String userId;
    }

    /**
     * Request payload for capturing a payment.
     */
    @Getter
    @Builder
    public static class CaptureRequest {
        private String orderId;
        private String userId;
    }

    /**
     * Response payload for a captured payment.
     */
    @Getter
    @Builder
    public static class CaptureResponse {
        private String orderId;
        private String gatewayName;
        private Boolean success;
        private String userId;
    }
}