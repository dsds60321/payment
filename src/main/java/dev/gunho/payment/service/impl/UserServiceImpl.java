package dev.gunho.payment.service.impl;

import dev.gunho.payment.model.dto.UserPayload;
import dev.gunho.payment.model.entity.Status;
import dev.gunho.payment.model.entity.UserEntity;
import dev.gunho.payment.repository.UserRepository;
import dev.gunho.payment.service.UserService;
import dev.gunho.payment.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public Mono<UserPayload.Response> createUser(UserPayload.Request request) {
        return mapToEntityWithPayKey(request)
                .flatMap(user -> {
                    log.debug("Saving new user with ID: {}", user.getUserId());
                    return userRepository.save(user); // 항상 INSERT로 동작
                })
                .flatMap(user -> {
                    UserPayload.Response response = UserPayload.Response.builder()
                            .userId(user.getUserId())
                            .payKey(user.getPayKey())
                            .regDate(user.getRegDate())
                            .build();
                    return Mono.just(response);
                });
    }


    private Mono<UserEntity> mapToEntityWithPayKey(UserPayload.Request request) {
        String userId = request.getUserId();
        if (Util.isNullOrEmpty(userId)) {
            return Mono.error(new IllegalArgumentException("userId is null or empty"));
        }

        return userRepository.existsByUserId(userId)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("userId is already exists"));
                    }

                    UserEntity user = UserEntity.builder()
                        .userId(userId) // 요청에서 받은 userId 사용
                        .status(Status.ACTIVE)
                        .payKey(getPayKey())
                        .build();

                    return Mono.just(user);
                });
    }



    private String getPayKey() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                UUID.randomUUID().toString().replace("-", "").substring(2, 5);
    }

}
