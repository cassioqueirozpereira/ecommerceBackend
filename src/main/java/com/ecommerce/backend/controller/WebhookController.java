package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.MercadoPagoWebhookDTO;
import com.ecommerce.backend.service.MercadoPagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final MercadoPagoService mercadoPagoService;

    @PostMapping("/mercadopago")
    public ResponseEntity<Void> handleMercadoPagoWebhook(@RequestBody MercadoPagoWebhookDTO payload) {
        mercadoPagoService.handleWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
