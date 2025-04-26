package dev.gunho.payment.handler.payment;

import dev.gunho.payment.model.dto.PaymentPayload;
import dev.gunho.payment.service.PaymentService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentHandlerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentHandler paymentHandler;

    private WebTestClient webTestClient;

    // 주문 생성 테스트 라우터
    private RouterFunction<ServerResponse> createOrderRoute() {
        return RouterFunctions.route()
                .POST("/payments/orders", request -> paymentHandler.createOrder(request))
                .build();
    }

    // 결제 캡처 테스트 라우터
    private RouterFunction<ServerResponse> capturePaymentRoute() {
        return RouterFunctions.route()
                .POST("/payments/capture", request -> paymentHandler.capturePayment(request))
                .build();
    }

    @Test
    @DisplayName("주문 생성 HTTP 요청 성공 테스트")
    void createOrder_Success() {
        // given
        String orderId = "ORDER-123456789";
        String userId = "testUser123";
        Double amount = 100.0;
        String currency = "USD";
        String description = "Test Order";
        String gatewayName = "PayPal";

        PaymentPayload.OrderRequest request = PaymentPayload.OrderRequest.builder()
                .amount(amount)
                .currency(currency)
                .description(description)
                .userId(userId)
                .build();

        when(paymentService.createOrder(anyDouble(), anyString(), anyString()))
                .thenReturn(Mono.just(orderId));
        when(paymentService.getGatewayName()).thenReturn(gatewayName);

        // WebTestClient 초기화
        webTestClient = WebTestClient
                .bindToRouterFunction(createOrderRoute())
                .build();

        // when/then
        webTestClient
                .post()
                .uri("/payments/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("/payments/orders/" + orderId)
                .expectBody()
                .jsonPath("$.orderId").isEqualTo(orderId)
                .jsonPath("$.gatewayName").isEqualTo(gatewayName)
                .jsonPath("$.amount").isEqualTo(amount)
                .jsonPath("$.currency").isEqualTo(currency)
                .jsonPath("$.description").isEqualTo(description)
                .jsonPath("$.userId").isEqualTo(userId);

        // verify
        verify(paymentService).createOrder(amount, currency, description);
        verify(paymentService).getGatewayName();
    }

    @Test
    @DisplayName("주문 생성 HTTP 요청 실패 테스트")
    void createOrder_Failure() {
        // given
        Double amount = 100.0;
        String currency = "USD";
        String description = "Test Order";
        String userId = "testUser123";

        PaymentPayload.OrderRequest request = PaymentPayload.OrderRequest.builder()
                .amount(amount)
                .currency(currency)
                .description(description)
                .userId(userId)
                .build();

        when(paymentService.createOrder(anyDouble(), anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Payment gateway error")));

        // WebTestClient 초기화
        webTestClient = WebTestClient
                .bindToRouterFunction(createOrderRoute())
                .build();

        // when/then
        webTestClient
                .post()
                .uri("/payments/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();

        // verify
        verify(paymentService).createOrder(amount, currency, description);
    }

    @Test
    @DisplayName("결제 캡처 HTTP 요청 성공 테스트")
    void capturePayment_Success() {
        // given
        String orderId = "ORDER-123456789";
        String userId = "testUser123";
        String gatewayName = "PayPal";

        PaymentPayload.CaptureRequest request = PaymentPayload.CaptureRequest.builder()
                .orderId(orderId)
                .userId(userId)
                .build();

        when(paymentService.capturePayment(anyString()))
                .thenReturn(Mono.just(true));
        when(paymentService.getGatewayName()).thenReturn(gatewayName);

        // WebTestClient 초기화
        webTestClient = WebTestClient
                .bindToRouterFunction(capturePaymentRoute())
                .build();

        // when/then
        webTestClient
                .post()
                .uri("/payments/capture")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.orderId").isEqualTo(orderId)
                .jsonPath("$.gatewayName").isEqualTo(gatewayName)
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.userId").isEqualTo(userId);

        // verify
        verify(paymentService).capturePayment(orderId);
        verify(paymentService).getGatewayName();
    }

    @Test
    @DisplayName("결제 캡처 HTTP 요청 실패 테스트 - 캡처 실패")
    void capturePayment_FailureCaptureFailed() {
        // given
        String orderId = "ORDER-123456789";
        String userId = "testUser123";
        String gatewayName = "PayPal";

        PaymentPayload.CaptureRequest request = PaymentPayload.CaptureRequest.builder()
                .orderId(orderId)
                .userId(userId)
                .build();

        when(paymentService.capturePayment(anyString()))
                .thenReturn(Mono.just(false));
        when(paymentService.getGatewayName()).thenReturn(gatewayName);

        // WebTestClient 초기화
        webTestClient = WebTestClient
                .bindToRouterFunction(capturePaymentRoute())
                .build();

        // when/then
        webTestClient
                .post()
                .uri("/payments/capture")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.orderId").isEqualTo(orderId)
                .jsonPath("$.gatewayName").isEqualTo(gatewayName)
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.userId").isEqualTo(userId);

        // verify
        verify(paymentService).capturePayment(orderId);
        verify(paymentService).getGatewayName();
    }

    @Test
    @DisplayName("결제 캡처 HTTP 요청 실패 테스트 - 서비스 오류")
    void capturePayment_FailureServiceError() {
        // given
        String orderId = "ORDER-123456789";
        String userId = "testUser123";

        PaymentPayload.CaptureRequest request = PaymentPayload.CaptureRequest.builder()
                .orderId(orderId)
                .userId(userId)
                .build();

        when(paymentService.capturePayment(anyString()))
                .thenReturn(Mono.error(new RuntimeException("Payment gateway error")));

        // WebTestClient 초기화
        webTestClient = WebTestClient
                .bindToRouterFunction(capturePaymentRoute())
                .build();

        // when/then
        webTestClient
                .post()
                .uri("/payments/capture")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();

        // verify
        verify(paymentService).capturePayment(orderId);
    }
}