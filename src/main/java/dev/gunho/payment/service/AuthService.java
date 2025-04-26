package dev.gunho.payment.service;

import dev.gunho.payment.model.entity.UserEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service for authentication-related operations.
 */
@Service
public interface AuthService {

    /**
     * Validates a user's payment key.
     *
     * @param userId The user ID
     * @param payKey The payment key to validate
     * @return A Mono containing true if the key is valid, false otherwise
     */
    Mono<Boolean> validatePayKey(String userId, String payKey);

    /**
     * Gets a user by their user ID.
     *
     * @param userId The user ID
     * @return A Mono containing the user entity, or empty if not found
     */
    Mono<UserEntity> getUserByUserId(String userId);
}
