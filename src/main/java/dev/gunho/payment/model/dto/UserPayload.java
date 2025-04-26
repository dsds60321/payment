package dev.gunho.payment.model.dto;

import dev.gunho.payment.model.entity.Status;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Data transfer objects for user operations.
 */
public class UserPayload {

    /**
     * Request payload for creating a user.
     */
    @Getter
    @Builder
    public static class Request {
        private String userId;
    }

    /**
     * Response payload for user operations.
     */
    @Getter
    @Builder
    public static class Response {
        private String userId;
        private String payKey;
        private Status status;
        private LocalDateTime regDate;
    }
}
