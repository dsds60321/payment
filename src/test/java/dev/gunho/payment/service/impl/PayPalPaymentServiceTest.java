package dev.gunho.payment.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayPalPaymentServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private PayPalPaymentService paymentService;
    private final String clientId = "test-client-id";
    private final String clientSecret = "test-client-secret";
    private final String baseUrl = "https://api-m.sandbox.paypal.com";

    @BeforeEach
    void setUp() {
        // WebClient 모킹 설정
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        // 토큰 요청 모킹
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/v1/oauth2/token")).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        // 토큰 응답 모킹
        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("access_token", "test-access-token");
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(tokenResponse));
        
        // PayPalPaymentService 인스턴스 생성
        paymentService = new PayPalPaymentService(webClientBuilder, clientId, clientSecret, baseUrl);
    }

    @Test
    @DisplayName("주문 생성 성공 시나리오")
    void createOrder_Success() {
        // given
        Double amount = 100.0;
        String currency = "USD";
        String description = "Test Order";
        String orderId = "ORDER-123456789";
        
        // 주문 생성 응답 모킹
        Map<String, Object> orderResponse = new HashMap<>();
        orderResponse.put("id", orderId);
        
        // 주문 생성 요청 모킹
        when(requestBodyUriSpec.uri("/v2/checkout/orders")).thenReturn(requestBodySpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(orderResponse));

        // when
        Mono<String> result = paymentService.createOrder(amount, currency, description);

        // then
        StepVerifier.create(result)
                .expectNext(orderId)
                .verifyComplete();
    }

    @Test
    @DisplayName("주문 생성 실패 시나리오")
    void createOrder_Failure() {
        // given
        Double amount = 100.0;
        String currency = "USD";
        String description = "Test Order";
        RuntimeException exception = new RuntimeException("API Error");
        
        // 주문 생성 요청 실패 모킹
        when(requestBodyUriSpec.uri("/v2/checkout/orders")).thenReturn(requestBodySpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(exception));

        // when
        Mono<String> result = paymentService.createOrder(amount, currency, description);

        // then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof RuntimeException && 
                    throwable.getMessage().equals("API Error"))
                .verify();
    }

    @Test
    @DisplayName("결제 캡처 성공 시나리오")
    void capturePayment_Success() {
        // given
        String orderId = "ORDER-123456789";
        
        // 결제 캡처 응답 모킹
        Map<String, Object> captureResponse = new HashMap<>();
        captureResponse.put("status", "COMPLETED");
        
        // 결제 캡처 요청 모킹
        when(requestBodyUriSpec.uri("/v2/checkout/orders/" + orderId + "/capture")).thenReturn(requestBodySpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(captureResponse));

        // when
        Mono<Boolean> result = paymentService.capturePayment(orderId);

        // then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("결제 캡처 실패 시나리오 - 상태가 COMPLETED가 아님")
    void capturePayment_FailureIncompleteStatus() {
        // given
        String orderId = "ORDER-123456789";
        
        // 결제 캡처 응답 모킹 (상태가 COMPLETED가 아님)
        Map<String, Object> captureResponse = new HashMap<>();
        captureResponse.put("status", "FAILED");
        
        // 결제 캡처 요청 모킹
        when(requestBodyUriSpec.uri("/v2/checkout/orders/" + orderId + "/capture")).thenReturn(requestBodySpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(captureResponse));

        // when
        Mono<Boolean> result = paymentService.capturePayment(orderId);

        // then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("결제 캡처 실패 시나리오 - API 오류")
    void capturePayment_FailureApiError() {
        // given
        String orderId = "ORDER-123456789";
        RuntimeException exception = new RuntimeException("API Error");
        
        // 결제 캡처 요청 실패 모킹
        when(requestBodyUriSpec.uri("/v2/checkout/orders/" + orderId + "/capture")).thenReturn(requestBodySpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(exception));

        // when
        Mono<Boolean> result = paymentService.capturePayment(orderId);

        // then
        StepVerifier.create(result)
                .expectNext(false) // onErrorReturn(false)로 인해 에러가 아닌 false 반환
                .verifyComplete();
    }

    @Test
    @DisplayName("게이트웨이 이름 반환 테스트")
    void getGatewayName() {
        // when
        String gatewayName = paymentService.getGatewayName();

        // then
        assertThat(gatewayName).isEqualTo("PayPal");
    }
}