package dev.gunho.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
public interface UserService {

    Mono<ServerResponse> createUser(ServerRequest request);

}
