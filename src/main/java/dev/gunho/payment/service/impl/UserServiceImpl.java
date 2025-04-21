package dev.gunho.payment.service.impl;

import dev.gunho.payment.model.dto.UserPayload;
import dev.gunho.payment.model.entity.Status;
import dev.gunho.payment.model.entity.UserEntity;
import dev.gunho.payment.model.mapper.UserMapper;
import dev.gunho.payment.repository.UserRepository;
import dev.gunho.payment.service.UserService;
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
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Mono<UserPayload.Response> createUser(UserPayload.Request request) {
        return userRepository.existsByUserId(request.getUserId())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("userId is already exists"));
                    }

                    // DTO -> Entity 반환
                    UserEntity userEntity = userMapper.toEntity(request)
                            .withStatus(Status.ACTIVE)
                            .withPayKey(getPayKey());

                    return userRepository.save(userEntity);
                })
                .map(userMapper::toDto);
    }

    protected String getPayKey() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                UUID.randomUUID().toString().replace("-", "").substring(2, 5);
    }

}
