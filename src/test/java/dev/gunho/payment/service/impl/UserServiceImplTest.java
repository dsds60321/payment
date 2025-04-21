package dev.gunho.payment.service.impl;

import dev.gunho.payment.model.dto.UserPayload;
import dev.gunho.payment.model.entity.Status;
import dev.gunho.payment.model.entity.UserEntity;
import dev.gunho.payment.model.mapper.UserMapper;
import dev.gunho.payment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity userEntityBeforeSave;
    private UserEntity userEntityAfterSave;
    private UserPayload.Request userRequest;
    private UserPayload.Response userResponse;
    private final String testUserId = "testUser123";

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        userRequest = UserPayload.Request.builder()
                .userId(testUserId)
                .build();

        userEntityBeforeSave = UserEntity.builder()
                .userId(testUserId)
                .build();

        userEntityAfterSave = UserEntity.builder()
                .idx(1L)
                .userId(testUserId)
                .status(Status.ACTIVE)
                .payKey("20240520123456abc")
                .regDate(LocalDateTime.now())
                .build();

        userResponse = UserPayload.Response.builder()
                .userId(testUserId)
                .status(Status.ACTIVE)
                .payKey("20240520123456abc")
                .regDate(LocalDateTime.now())
                .build();
    }


    @Test
    @DisplayName("유저 생성 성공 시나리오")
    void createUser_Success() {
        // given
        when(userRepository.existsByUserId(testUserId)).thenReturn(Mono.just(false));
        when(userMapper.toEntity(userRequest)).thenReturn(userEntityBeforeSave);
        when(userRepository.save(any(UserEntity.class))).thenReturn(Mono.just(userEntityAfterSave));
        when(userMapper.toDto(userEntityAfterSave)).thenReturn(userResponse);

        // when
        Mono<UserPayload.Response> result = userService.createUser(userRequest);

        // then
        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assertThat(response.getUserId()).isEqualTo(testUserId);
                    assertThat(response.getStatus()).isEqualTo(Status.ACTIVE);
                    assertThat(response.getPayKey()).isNotNull();
                    return true;
                })
                .verifyComplete();

        // verify
        verify(userRepository).existsByUserId(testUserId);
        verify(userMapper).toEntity(userRequest);

        // UserEntity 저장 시 withStatus, withPayKey 메서드 호출 확인
        ArgumentCaptor<UserEntity> entityCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(entityCaptor.capture());
        UserEntity capturedEntity = entityCaptor.getValue();

        assertThat(capturedEntity.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(capturedEntity.getPayKey()).isNotNull();

        verify(userMapper).toDto(userEntityAfterSave);
    }

    @Test
    @DisplayName("이미 존재하는 유저ID로 생성 요청 시 예외 발생")
    void createUser_AlreadyExists() {
        // given
        when(userRepository.existsByUserId(testUserId)).thenReturn(Mono.just(true));

        // when
        Mono<UserPayload.Response> result = userService.createUser(userRequest);

        // then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("userId is already exists"))
                .verify();

        // verify: 사용자가 이미 존재하므로 저장이 호출되면 안됨
        verify(userRepository).existsByUserId(testUserId);
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("getPayKey를 통해 생성된 키의 형식 검증")
    void validatePayKeyFormat() {
        // when - protected 메서드 직접 호출
        String payKey = userService.getPayKey();

        // then
        assertThat(payKey).isNotNull();
        // 형식 검증: yyyyMMddHHmmss + UUID 3자리
        assertThat(payKey).matches("\\d{14}[a-f0-9]{3}");
    }
}