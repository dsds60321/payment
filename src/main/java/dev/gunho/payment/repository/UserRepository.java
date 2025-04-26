package dev.gunho.payment.repository;

import dev.gunho.payment.model.entity.UserEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Repository for user entities.
 */
@Repository
public interface UserRepository extends ReactiveCrudRepository<UserEntity, Long> {
    /**
     * Checks if a user with the given user ID exists.
     *
     * @param userId The user ID to check
     * @return A Mono containing true if the user exists, false otherwise
     */
    Mono<Boolean> existsByUserId(String userId);

    /**
     * Finds a user by their user ID.
     *
     * @param userId The user ID to search for
     * @return A Mono containing the user entity, or empty if not found
     */
    Mono<UserEntity> findByUserId(String userId);
}
