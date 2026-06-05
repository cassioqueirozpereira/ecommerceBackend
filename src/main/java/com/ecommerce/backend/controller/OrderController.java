package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.OrderRequestDTO;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.security.CustomUserDetails;
import com.ecommerce.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Value("${cron.secret}")
    private String cronSecret;

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @Valid @RequestBody OrderRequestDTO orderRequestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        Order order = orderService.createOrder(userDetails.getUser().getId(), orderRequestDTO, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    // --- Admin endpoints ---

    @GetMapping("/admin/pending-pix")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getPendingPixOrders() {
        return ResponseEntity.ok(orderService.getPendingPixOrders());
    }

    @PostMapping("/admin/{id}/approve-pix")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> approvePix(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Order order = orderService.approvePix(id, userDetails.getUser().getId());
        return ResponseEntity.ok(order);
    }

    @PostMapping("/admin/{id}/reject-pix")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> rejectPix(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Order order = orderService.rejectPix(id, userDetails.getUser().getId());
        return ResponseEntity.ok(order);
    }

    // --- External cron job endpoint (protected by secret header) ---

    @PostMapping("/internal/expire")
    public ResponseEntity<Map<String, Object>> expireOrders(
            @RequestHeader("X-Cron-Secret") String secret) {

        if (!cronSecret.equals(secret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid cron secret"));
        }

        int count = orderService.expireOldPixOrders();
        return ResponseEntity.ok(Map.of("expired", count, "status", "ok"));
    }
}