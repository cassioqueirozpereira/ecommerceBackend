package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.MercadoPagoWebhookDTO;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.WebhookEvent;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.WebhookEventRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MercadoPagoService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoService.class);
    private final WebhookEventRepository webhookEventRepository;
    private final OrderRepository orderRepository;

    @Value("${mercadopago.access-token}")
    private String accessToken;

    public String createCheckoutPreference(Order order) {
        if (accessToken == null || accessToken.trim().isEmpty() || accessToken.contains("your_access_token")) {
            logger.info("Mercado Pago access token not configured. Returning simulated sandbox checkout URL.");
            return "https://www.mercadopago.com.br/sandbox/payments/simulated-checkout?orderId=" + order.getId();
        }

        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            List<PreferenceItemRequest> items = order.getItems().stream().map(item -> 
                PreferenceItemRequest.builder()
                    .id(item.getId().toString())
                    .title(item.getProductName() + " - " + item.getVariantName())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .build()
            ).collect(Collectors.toList());

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .externalReference(order.getId().toString())
                .notificationUrl("https://your-domain.com/api/webhooks/mercadopago")
                .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            return preference.getInitPoint();
        } catch (Exception e) {
            logger.error("Error creating Mercado Pago checkout preference: ", e);
            return "https://www.mercadopago.com.br/sandbox/payments/simulated-checkout?orderId=" + order.getId();
        }
    }

    @Transactional
    public void handleWebhook(MercadoPagoWebhookDTO payload) {
        String eventId = payload.getId() != null ? payload.getId().toString() : "UNKNOWN_ID";

        // IDEMPOTENCY CHECK
        if (webhookEventRepository.existsByExternalId(eventId)) {
            logger.info("Webhook event {} already processed. Skipping.", eventId);
            return;
        }

        logger.info("Processing webhook event: {}", eventId);

        if (payload.getType() != null && payload.getType().equalsIgnoreCase("payment")) {
            String paymentId = payload.getData() != null ? payload.getData().getId() : null;
            if (paymentId != null) {
                processPaymentStatus(paymentId);
            }
        }

        // Mark as processed
        WebhookEvent event = new WebhookEvent();
        event.setExternalId(eventId);
        event.setType(payload.getType() != null ? payload.getType() : "unknown");
        event.setPayload(payload.getData());
        
        webhookEventRepository.save(event);
    }

    private void processPaymentStatus(String paymentId) {
        if (accessToken == null || accessToken.trim().isEmpty() || accessToken.contains("your_access_token")) {
            logger.info("Skipping real MP payment fetch - token not configured.");
            return;
        }

        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment = client.get(Long.parseLong(paymentId));

            String externalRef = mpPayment.getExternalReference();
            if (externalRef != null) {
                UUID orderId = UUID.fromString(externalRef);
                Order order = orderRepository.findById(orderId).orElse(null);
                if (order != null) {
                    String status = mpPayment.getStatus();
                    if ("approved".equalsIgnoreCase(status)) {
                        order.setStatus("PAID");
                    } else if ("rejected".equalsIgnoreCase(status)) {
                        order.setStatus("FAILED");
                    } else if ("cancelled".equalsIgnoreCase(status)) {
                        order.setStatus("CANCELLED");
                    }
                    orderRepository.save(order);
                    logger.info("Updated Order {} status to {} via MP payment {}", orderId, order.getStatus(), paymentId);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching/processing MP payment status: ", e);
        }
    }
}
