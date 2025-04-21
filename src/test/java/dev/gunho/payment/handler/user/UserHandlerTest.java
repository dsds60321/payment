package dev.gunho.payment.handler.user;

import dev.gunho.payment.model.dto.UserPayload;
import dev.gunho.payment.model.entity.Status;
import dev.gunho.payment.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserHandlerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserHandler userHandler;

    private WebTestClient webTestClient;

    // 테스트 라우터 생성
    private RouterFunction<ServerResponse> createUserRoute() {
        return RouterFunctions.route()
                .POST("/users", request -> userHandler.createUser(request))
                .build();
    }

    @Test
    @DisplayName("유저 생성 HTTP 요청 테스트")
    void createUser_endpoint() {
        // given
        String userId = "testUser123";
        UserPayload.Request request = UserPayload.Request.builder()
                .userId(userId)
                .build();

        UserPayload.Response response = UserPayload.Response.builder()
                .userId(userId)
                .status(Status.ACTIVE)
                .payKey("20240520123456abc")
                .regDate(LocalDateTime.now())
                .build();

        when(userService.createUser(any(UserPayload.Request.class)))
                .thenReturn(Mono.just(response));

        // WebTestClient 초기화
        webTestClient = WebTestClient
                .bindToRouterFunction(createUserRoute())
                .build();

        // when/then
        webTestClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("/users/" + userId)
                .expectBody()
                .jsonPath("$.userId").isEqualTo(userId)
                .jsonPath("$.status").isEqualTo(Status.ACTIVE.toString())
                .jsonPath("$.payKey").isEqualTo("20240520123456abc");

        // verify
        verify(userService).createUser(any(UserPayload.Request.class));
    }

    @Test
    @DisplayName("유저 생성 에러 핸들링 테스트")
    void createUser_withError() {
        // given
        String userId = "existingUser";
        UserPayload.Request request = UserPayload.Request.builder()
                .userId(userId)
                .build();

        when(userService.createUser(any(UserPayload.Request.class)))
                .thenReturn(Mono.error(new IllegalArgumentException("userId is already exists")));

        // WebTestClient 초기화
        webTestClient = WebTestClient
                .bindToRouterFunction(createUserRoute())
                .build();

        // when/then
        webTestClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError() // 예외 처리에 따라 상태 코드 변경
                .expectBody()
                .jsonPath("$.message").isEqualTo("userId is already exists");
    }

    @Test
    @DisplayName("유저 생성 핸들러 로직 단위 테스트")
    void createUser_handlerLogic() {
        // given
        String userId = "testUser123";
        UserPayload.Request requestDto = UserPayload.Request.builder()
                .userId(userId)
                .build();

        UserPayload.Response responseDto = UserPayload.Response.builder()
                .userId(userId)
                .status(Status.ACTIVE)
                .payKey("20240520123456abc")
                .regDate(LocalDateTime.now())
                .build();

        when(userService.createUser(any(UserPayload.Request.class)))
                .thenReturn(Mono.just(responseDto));

        // 테스트용 ServerRequest 생성은 복잡하므로 WebTestClient 사용
        webTestClient = WebTestClient
                .bindToRouterFunction(createUserRoute())
                .build();

        // when - 요청 실행
        webTestClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserPayload.Response.class)
                .isEqualTo(responseDto);
    }
}