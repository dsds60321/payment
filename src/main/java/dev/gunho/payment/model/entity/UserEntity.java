package dev.gunho.payment.model.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "user")
public class UserEntity {

    @Id
    private Long idx;
    private String userId;
    private Status status;
    private String payKey;
    private LocalDateTime regDate;


    public UserEntity withStatus(Status status) {
        return this.toBuilder()
                .status(status)
                .build();
    }

    public UserEntity withPayKey(String payKey) {
        return this.toBuilder()
                .payKey(payKey)
                .build();
    }

}