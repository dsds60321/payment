package dev.gunho.payment.service.impl;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Scanner;

public class PayPalApiExample {

    private static final String PAYPAL_API_BASE_URL = "https://api-m.sandbox.paypal.com";
    private static final String CLIENT_ID = "Aaw7KXPvkPSx6mPDthIxbGcYBqpC4AGVHv67YWmamviKnymDsf-9dLVROz-rmutbNReivBmhMoYg3d0k"; // 당신의 클라이언트 ID
    private static final String CLIENT_SECRET = "EC8Shmqa_4dPI9PyCgVqdZsq47huqsw8B31eqiQ0-VP7GZHb5i86OmS6v_LyqNn7DZT2I4kNh1VtMb2R"; // 당신의 클라이언트 시크릿


    public static void main(String[] args) {
        try {
            String accessToken = PayPalApiExample.getAccessToken(); // 앞서 작성한 인증 함수 사용
            String orderId = createOrder(accessToken);
            captureOrder(orderId, accessToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getAccessToken() throws Exception {
        String authUrl = PAYPAL_API_BASE_URL + "/v1/oauth2/token";

        // 클라이언트 ID와 시크릿을 Base64 인코딩
        String encodedCredentials = Base64.getEncoder()
                .encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes());

        URL url = new URL(authUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);

        // 요청 본문에 grant_type 파라미터 추가
        String payload = "grant_type=client_credentials";
        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.getBytes());
            os.flush();
        }

        // 응답 처리
        if (connection.getResponseCode() == 200) {
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                StringBuilder response = new StringBuilder();
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
                // JSON 파싱 (간단한 방법으로 처리, 라이브러리 사용 권장)
                String token = response.toString();
                System.out.printf("Access Token: %s%n", token);
                int startIndex = token.indexOf("access_token\":\"") + "access_token\":\"".length();
                int endIndex = token.indexOf("\"", startIndex);
                return token.substring(startIndex, endIndex);
            }
        } else {
            throw new RuntimeException("Failed to get access token: HTTP " + connection.getResponseCode());
        }
    }

    // 샘플 API 호출: 사용자 정보 조회 (GET 요청)
    private static void getUserInfo(String accessToken) throws Exception {
        String userInfoUrl = PAYPAL_API_BASE_URL + "/v1/identity/oauth2/userinfo?schema=paypalv1.1";

        URL url = new URL(userInfoUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);

        // 응답 처리
        if (connection.getResponseCode() == 200) {
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                StringBuilder response = new StringBuilder();
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
                System.out.println("User Info: " + response);
            }
        } else {
            throw new RuntimeException("Failed to get user info: HTTP " + connection.getResponseCode());
        }
    }


    // 1. 주문 생성
    private static String createOrder(String accessToken) throws Exception {
        URL url = new URL(PAYPAL_API_BASE_URL + "/v2/checkout/orders");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // 주문 요청 JSON
        String payload = """
                {
                  "intent": "CAPTURE",
                  "purchase_units": [{
                    "amount": {
                      "currency_code": "USD",
                      "value": "10.00"
                    }
                  }]
                }
                """;

        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.getBytes());
            os.flush();
        }

        // 응답 처리
        if (connection.getResponseCode() == 201) {
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                StringBuilder response = new StringBuilder();
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
                System.out.println("Order Created: " + response);

                // JSON 응답에서 order_id 추출 (간단히 처리)
                String orderId = response.substring(response.indexOf("\"id\":\"") + 6, response.indexOf("\"", response.indexOf("\"id\":\"") + 6));
                System.out.println("Order ID: " + orderId);
                return orderId;
            }
        } else {
            throw new RuntimeException("Failed to create order: HTTP " + connection.getResponseCode());
        }
    }

    // 2. 결제 캡처
    private static void captureOrder(String orderId, String accessToken) throws Exception {
        URL url = new URL(PAYPAL_API_BASE_URL + "/v2/checkout/orders/" + orderId + "/capture");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // 응답 처리
        if (connection.getResponseCode() == 201) {
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                StringBuilder response = new StringBuilder();
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
                System.out.println("Order Captured: " + response);
            }
        } else {
            throw new RuntimeException("Failed to capture order: HTTP " + connection.getResponseCode());
        }
    }

}