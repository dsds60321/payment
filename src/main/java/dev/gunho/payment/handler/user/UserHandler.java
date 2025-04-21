package dev.gunho.payment.handler.user;

import dev.gunho.payment.model.dto.UserPayload;
import dev.gunho.payment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class UserHandler {

    private final UserService userService;

    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(UserPayload.Request.class)
                .flatMap(userService::createUser)
                .flatMap(savedUser -> ServerResponse.created(URI.create("/users/" + savedUser.getUserId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedUser));
    }
}
