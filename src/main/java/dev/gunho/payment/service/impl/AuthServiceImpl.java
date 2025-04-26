package dev.gunho.payment.service.impl;

import dev.gunho.payment.model.entity.Status;
import dev.gunho.payment.model.entity.UserEntity;
import dev.gunho.payment.repository.UserRepository;
import dev.gunho.payment.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the AuthService interface.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    public Mono<Boolean> validatePayKey(String userId, String payKey) {
        if (userId == null || payKey == null) {
            return Mono.just(false);
        }

        return userRepository.findByUserId(userId)
                .map(user -> {
                    boolean isValid = user.getStatus() == Status.ACTIVE && 
                                     payKey.equals(user.getPayKey());

                    if (!isValid) {
                        log.warn("Invalid payKey for user {}", userId);
                    }

                    return isValid;
                })
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<UserEntity> getUserByUserId(String userId) {
        if (userId == null) {
            return Mono.empty();
        }

        return userRepository.findByUserId(userId)
                .doOnNext(user -> log.info("Found user: {}", user.getUserId()))
                .doOnError(e -> log.error("Error finding user: {}", e.getMessage()));
    }
}
