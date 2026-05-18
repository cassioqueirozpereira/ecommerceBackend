package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.OrderRequestDTO;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.security.CustomUserDetails;
import com.ecommerce.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        Order order = orderService.createOrder(userDetails.getUser().getId(), orderRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}