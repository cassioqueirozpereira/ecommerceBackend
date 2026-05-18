package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.MercadoPagoWebhookDTO;
import com.ecommerce.backend.entity.WebhookEvent;
import com.ecommerce.backend.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MercadoPagoService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoService.class);
    private final WebhookEventRepository webhookEventRepository;

    @Transactional
    public void handleWebhook(MercadoPagoWebhookDTO payload) {
        String eventId = payload.getId() != null ? payload.getId().toString() : "UNKNOWN_ID";

        // IDEMPOTENCY CHECK
        if (webhookEventRepository.existsByExternalId(eventId)) {
            logger.info("Webhook event {} already processed. Skipping.", eventId);
            return;
        }

        logger.info("Processing webhook event: {}", eventId);

        // TODO: Handle Mercado Pago payment status updates
        // For example, if payload.getType().equals("payment")
        // Fetch payment info from MercadoPago SDK, update local Order and Payment status.

        // Mark as processed
        WebhookEvent event = new WebhookEvent();
        event.setExternalId(eventId);
        event.setType(payload.getType());
        event.setPayload(payload.getData());
        
        webhookEventRepository.save(event);
    }
}
