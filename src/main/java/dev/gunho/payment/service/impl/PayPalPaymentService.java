package dev.gunho.payment.service.impl;

import dev.gunho.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * PayPal implementation of the PaymentService interface.
 * This class handles payment processing through the PayPal API.
 */
@Slf4j
@Service
public class PayPalPaymentService implements PaymentService {

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;
    private final String baseUrl;

    public PayPalPaymentService(
            WebClient.Builder webClientBuilder,
            @Value("${api.paypal.client}") String clientId,
            @Value("${api.paypal.secret}") String clientSecret,
            @Value("${api.paypal.base-url:https://api-m.sandbox.paypal.com}") String baseUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.baseUrl = baseUrl;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<String> createOrder(Double amount, String currency, String description) {
        return getAccessToken()
                .flatMap(token -> {
                    Map<String, Object> orderRequest = new HashMap<>();
                    orderRequest.put("intent", "CAPTURE");
                    
                    Map<String, Object> amountMap = new HashMap<>();
                    amountMap.put("currency_code", currency);
                    amountMap.put("value", amount.toString());
                    
                    Map<String, Object> purchaseUnit = new HashMap<>();
                    purchaseUnit.put("amount", amountMap);
                    if (description != null && !description.isEmpty()) {
                        purchaseUnit.put("description", description);
                    }
                    
                    orderRequest.put("purchase_units", new Object[]{purchaseUnit});
                    
                    return webClient.post()
                            .uri("/v2/checkout/orders")
                            .header("Authorization", "Bearer " + token)
                            .header("Content-Type", "application/json")
                            .bodyValue(orderRequest)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .map(response -> (String) response.get("id"))
                            .doOnError(e -> log.error("Error creating PayPal order: {}", e.getMessage()));
                });
    }

    @Override
    public Mono<Boolean> capturePayment(String orderId) {
        return getAccessToken()
                .flatMap(token -> webClient.post()
                        .uri("/v2/checkout/orders/" + orderId + "/capture")
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .retrieve()
                        .bodyToMono(Map.class)
                        .map(response -> {
                            String status = (String) response.get("status");
                            return "COMPLETED".equals(status);
                        })
                        .doOnError(e -> log.error("Error capturing PayPal payment: {}", e.getMessage()))
                        .onErrorReturn(false));
    }

    @Override
    public String getGatewayName() {
        return "PayPal";
    }

    /**
     * Gets an access token from the PayPal API.
     *
     * @return A Mono containing the access token
     */
    private Mono<String> getAccessToken() {
        String credentials = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        
        return webClient.post()
                .uri("/v1/oauth2/token")
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("grant_type=client_credentials")
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"))
                .doOnError(e -> log.error("Error getting PayPal access token: {}", e.getMessage()));
    }
}