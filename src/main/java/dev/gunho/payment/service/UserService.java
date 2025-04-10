package dev.gunho.payment.service;

import dev.gunho.payment.model.dto.UserPayload;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public interface UserService {

    Mono<UserPayload.Response> createUser(UserPayload.Request user);

}
