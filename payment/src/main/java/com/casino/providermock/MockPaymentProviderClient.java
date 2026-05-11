package com.casino.providermock;

import com.casino.dto.PaymentProviderWebhookDto;
import com.casino.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class MockPaymentProviderClient {

    private final RestTemplate restTemplate;
    private final Random random = new Random();

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    @Value("${mock.payment.delay-seconds:5}")
    private long delaySeconds;

    @Async("paymentMockExecutor")
    public void simulateProviderCallback(Long paymentId, String provider) {
        try {
            Thread.sleep(Duration.ofSeconds(delaySeconds).toMillis());

            PaymentStatus finalStatus = random.nextDouble() < 0.85
                ? PaymentStatus.COMPLETED
                : PaymentStatus.FAILED;

            PaymentProviderWebhookDto webhookDto = new PaymentProviderWebhookDto(
                paymentId,
                provider,
                "",
                finalStatus,
                "Mock provider callback finished with status: " + finalStatus
            );

            String webhookUrl = appBaseUrl + "/payments/provider/webhook";

            restTemplate.postForEntity(
                webhookUrl,
                webhookDto,
                Void.class
            );

            log.info("Mock provider callback sent for paymentId={} with status={}", paymentId, finalStatus);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Mock payment callback interrupted for paymentId={}", paymentId);
        } catch (Exception e) {
            log.error("Failed to send mock payment callback for paymentId={}", paymentId, e);
        }
    }
}