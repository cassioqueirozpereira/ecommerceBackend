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

    public com.mercadopago.resources.payment.Payment processCardPayment(Order order, com.ecommerce.backend.dto.CardPaymentDTO cardDTO) {
        if (accessToken == null || accessToken.trim().isEmpty() || accessToken.contains("your_access_token")) {
            logger.error("Mercado Pago access token not configured.");
            throw new RuntimeException("Configuração do Mercado Pago ausente. Contate o suporte.");
        }

        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            com.mercadopago.client.common.IdentificationRequest identRequest = null;
            if (cardDTO.getPayer().getIdentification() != null
                    && cardDTO.getPayer().getIdentification().getType() != null
                    && !cardDTO.getPayer().getIdentification().getType().isBlank()) {
                identRequest = com.mercadopago.client.common.IdentificationRequest.builder()
                    .type(cardDTO.getPayer().getIdentification().getType())
                    .number(cardDTO.getPayer().getIdentification().getNumber())
                    .build();
            }

            com.mercadopago.client.payment.PaymentPayerRequest payerRequest =
                com.mercadopago.client.payment.PaymentPayerRequest.builder()
                    .email(cardDTO.getPayer().getEmail())
                    .identification(identRequest)
                    .build();

            com.mercadopago.client.payment.PaymentCreateRequest createRequest = com.mercadopago.client.payment.PaymentCreateRequest.builder()
                .transactionAmount(order.getTotal())
                .token(cardDTO.getToken())
                .description("Order " + order.getId().toString())
                .installments(cardDTO.getInstallments())
                .paymentMethodId(cardDTO.getPaymentMethodId())
                .issuerId(cardDTO.getIssuerId())
                .payer(payerRequest)
                .externalReference(order.getId().toString())
                .build();

            PaymentClient client = new PaymentClient();
            return client.create(createRequest);
        } catch (com.mercadopago.exceptions.MPApiException e) {
            logger.error("MP API Exception creating card payment: status={}, content={}", e.getStatusCode(), e.getApiResponse().getContent());
            String content = e.getApiResponse().getContent();
            String code = "unknown_api_error";
            
            // Try to extract the cause code if present
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"code\"\\s*:\\s*\"?([a-zA-Z0-9]+)\"?");
            java.util.regex.Matcher m = p.matcher(content);
            if (m.find()) {
                code = "api_" + m.group(1); // prefix with api_ to distinguish from cc_rejected
            } else if (content.contains("invalid_cvv") || content.contains("security_code")) {
                code = "cc_rejected_bad_filled_cvv";
            }
            
            throw new com.ecommerce.backend.exception.PaymentRejectedException(code, "Erro na validação do pagamento: " + code);
            
        } catch (com.mercadopago.exceptions.MPException e) {
            logger.error("MP Exception creating card payment: ", e);
            throw new RuntimeException("Falha de comunicação com o Mercado Pago", e);
        } catch (Exception e) {
            logger.error("Unexpected error creating Mercado Pago card payment: ", e);
            throw new RuntimeException("Falha inesperada ao processar pagamento com cartão", e);
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
