package dev.gunho.payment.model.dto;

import dev.gunho.payment.model.entity.Status;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class UserPayload {

    @Getter
    @Builder
    public static class Request {
        private String userId;
    }

    @Getter
    public static class Response {
        private String userId;
        private String payKey;
        private Status status;
        private LocalDateTime regDate;
    }
}
