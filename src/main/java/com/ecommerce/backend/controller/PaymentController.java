package com.ecommerce.backend.controller;

import com.ecommerce.backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/preference/{orderId}")
    public ResponseEntity<String> createPreference(@PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.createPaymentPreference(orderId));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody Map<String, Object> payload) {
        paymentService.processWebhook(payload);
        return ResponseEntity.ok().build();
    }
}