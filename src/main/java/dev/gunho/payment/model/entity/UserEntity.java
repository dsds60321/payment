package dev.gunho.payment.model.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Builder
@Table(name = "user")
public class UserEntity {

    @Id
    private Long idx;
    private String userId;
    private Status status;
    private String payKey;
    private LocalDateTime regDate;
}