package dev.gunho.payment.repository;

import dev.gunho.payment.model.entity.UserEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<UserEntity, String> {
    Mono<Boolean> existsByUserId(String userId);
}
