package dev.gunho.payment.service.impl;

import dev.gunho.payment.model.entity.Status;
import dev.gunho.payment.model.entity.UserEntity;
import dev.gunho.payment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserEntity activeUser;
    private UserEntity inactiveUser;
    private final String userId = "testUser123";
    private final String payKey = "20240520123456abc";

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        activeUser = UserEntity.builder()
                .idx(1L)
                .userId(userId)
                .status(Status.ACTIVE)
                .payKey(payKey)
                .regDate(LocalDateTime.now())
                .build();

        inactiveUser = UserEntity.builder()
                .idx(2L)
                .userId("inactiveUser")
                .status(Status.INACTIVE)
                .payKey("differentPayKey")
                .regDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("유효한 payKey 검증 성공 시나리오")
    void validatePayKey_ValidKey_Success() {
        // given
        when(userRepository.findByUserId(userId)).thenReturn(Mono.just(activeUser));

        // when
        Mono<Boolean> result = authService.validatePayKey(userId, payKey);

        // then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        // verify
        verify(userRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("유효하지 않은 payKey 검증 실패 시나리오")
    void validatePayKey_InvalidKey_Failure() {
        // given
        when(userRepository.findByUserId(userId)).thenReturn(Mono.just(activeUser));

        // when
        Mono<Boolean> result = authService.validatePayKey(userId, "invalidPayKey");

        // then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        // verify
        verify(userRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("비활성 상태 사용자의 payKey 검증 실패 시나리오")
    void validatePayKey_InactiveUser_Failure() {
        // given
        when(userRepository.findByUserId("inactiveUser")).thenReturn(Mono.just(inactiveUser));

        // when
        Mono<Boolean> result = authService.validatePayKey("inactiveUser", "differentPayKey");

        // then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        // verify
        verify(userRepository).findByUserId("inactiveUser");
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 payKey 검증 실패 시나리오")
    void validatePayKey_UserNotFound_Failure() {
        // given
        when(userRepository.findByUserId("nonExistentUser")).thenReturn(Mono.empty());

        // when
        Mono<Boolean> result = authService.validatePayKey("nonExistentUser", payKey);

        // then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        // verify
        verify(userRepository).findByUserId("nonExistentUser");
    }

    @Test
    @DisplayName("null 입력값에 대한 payKey 검증 실패 시나리오")
    void validatePayKey_NullInputs_Failure() {
        // when & then - null userId
        StepVerifier.create(authService.validatePayKey(null, payKey))
                .expectNext(false)
                .verifyComplete();

        // when & then - null payKey
        StepVerifier.create(authService.validatePayKey(userId, null))
                .expectNext(false)
                .verifyComplete();

        // when & then - both null
        StepVerifier.create(authService.validatePayKey(null, null))
                .expectNext(false)
                .verifyComplete();

        // verify - repository should not be called with null userId
        verify(userRepository, never()).findByUserId(null);
    }

    @Test
    @DisplayName("사용자 ID로 사용자 조회 성공 시나리오")
    void getUserByUserId_Success() {
        // given
        when(userRepository.findByUserId(userId)).thenReturn(Mono.just(activeUser));

        // when
        Mono<UserEntity> result = authService.getUserByUserId(userId);

        // then
        StepVerifier.create(result)
                .expectNext(activeUser)
                .verifyComplete();

        // verify
        verify(userRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 조회 시 빈 결과 반환")
    void getUserByUserId_UserNotFound_EmptyResult() {
        // given
        when(userRepository.findByUserId("nonExistentUser")).thenReturn(Mono.empty());

        // when
        Mono<UserEntity> result = authService.getUserByUserId("nonExistentUser");

        // then
        StepVerifier.create(result)
                .verifyComplete(); // empty result

        // verify
        verify(userRepository).findByUserId("nonExistentUser");
    }

    @Test
    @DisplayName("null 사용자 ID로 조회 시 빈 결과 반환")
    void getUserByUserId_NullUserId_EmptyResult() {
        // when
        Mono<UserEntity> result = authService.getUserByUserId(null);

        // then
        StepVerifier.create(result)
                .verifyComplete(); // empty result

        // verify - repository should not be called with null userId
        verify(userRepository, never()).findByUserId(null);
    }
}