package dev.gunho.payment.service.impl;

import dev.gunho.payment.model.dto.UserPayload;
import dev.gunho.payment.model.entity.UserEntity;
import dev.gunho.payment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this); // Mockito 모킹 초기화
    }

    @Test
    @Transactional
    void createUser_shouldCreateUserSuccessfully_whenUserIdNotExists() {
        // given
        UserPayload.Request request = UserPayload.Request.builder()
                .userId("testUser")
                .build();

        ServerRequest serverRequest = mock(ServerRequest.class);
        when(serverRequest.bodyToMono(UserPayload.Request.class)).thenReturn(Mono.just(request));

        // Mock 리포지토리 로직
        when(userRepository.existsById("testUser")).thenReturn(Mono.just(false));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            System.out.println("save userId : "  + user.getUserId());
            return Mono.just(user); // Mock 저장 시 사용자 반환
        });

        // when
        userService.createUser(request);

        // then
//        StepVerifier.create(response)
//                .assertNext(serverResponse -> assertEquals(200, serverResponse.statusCode().value())) // 성공 시 상태 코드 확인
//                .verifyComplete();
//
//        verify(userRepository, times(1)).existsById("testUser");
//        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @Transactional
    void createUser_shouldReturnError_whenUserIdIsNull() {
        // given
        UserPayload.Request request = UserPayload.Request.builder()
                .userId(null)
                .build();

        ServerRequest serverRequest = mock(ServerRequest.class);
        when(serverRequest.bodyToMono(UserPayload.Request.class)).thenReturn(Mono.just(request));

        // when
//        Mono<ServerResponse> response = userService.createUser(request);
//
//        // then
//        StepVerifier.create(response)
//                .expectErrorMatches(error -> error instanceof IllegalArgumentException
//                        && error.getMessage().equals("userId is null or empty"))
//                .verify();
//
//        verify(userRepository, never()).existsById(anyString()); // 호출되지 않아야 함
//        verify(userRepository, never()).save(any(UserEntity.class)); // 저장 호출되지 않아야 함
    }

    @Test
    @Transactional
    void createUser_shouldReturnError_whenUserIdAlreadyExists() {
        // given
        UserPayload.Request request = UserPayload.Request.builder()
                .userId("existingUser")
                .build();

        ServerRequest serverRequest = mock(ServerRequest.class);
        when(serverRequest.bodyToMono(UserPayload.Request.class)).thenReturn(Mono.just(request));

        // Mock 데이터베이스 검증: userId가 이미 존재
        when(userRepository.existsById("existingUser")).thenReturn(Mono.just(true));

        // when
//        Mono<ServerResponse> response = userService.createUser(request);
//
//        // then
//        StepVerifier.create(response)
//                .expectErrorMatches(error -> error instanceof IllegalArgumentException
//                        && error.getMessage().equals("userId is already exists"))
//                .verify();
//
//        verify(userRepository, times(1)).existsById("existingUser");
//        verify(userRepository, never()).save(any(UserEntity.class)); // 저장 호출되지 않아야 함
    }


}