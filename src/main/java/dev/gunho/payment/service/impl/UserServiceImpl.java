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
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
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
    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(UserPayload.Request.class)
                .flatMap(this::mapToEntityWithPayKey)
                .flatMap(userRepository::save)
                .flatMap(user -> ServerResponse.ok().bodyValue(user))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    private Mono<UserEntity> mapToEntityWithPayKey(UserPayload.Request request) {
        String userId = request.getUserId();
        if (Util.isNullOrEmpty(userId)) {
            return Mono.error(new IllegalArgumentException("userId is null or empty"));
        }

        return userRepository.existsById(userId)
                .flatMap(exists -> {
                    if(exists) {
                        return Mono.error(new IllegalArgumentException("userId is already exists"));
                    }
                    UserEntity user = UserEntity.builder()
                            .userId(request.getUserId())
                            .payKey(getPayKey())
                            .status(Status.ACTIVE)
                            .build();

                    return Mono.just(user);
                });
    }


    private String getPayKey() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                UUID.randomUUID().toString().replace("-", "").substring(2, 5);
    }

}
