package dev.gunho.payment.repository;

import dev.gunho.payment.model.entity.UserEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ReactiveCrudRepository<UserEntity, String> {
    UserEntity findByPayKey(String paykey);

    boolean existsByPayKey(String payKey);
}
