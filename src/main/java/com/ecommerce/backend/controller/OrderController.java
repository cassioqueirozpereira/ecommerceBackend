package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.OrderDTOs;
import com.ecommerce.backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDTOs.OrderResponse> createOrder(@RequestBody OrderDTOs.OrderRequest request, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(orderService.createOrder(email, request));
    }

    @GetMapping
    public ResponseEntity<List<OrderDTOs.OrderResponse>> getUserOrders(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(orderService.getUserOrders(email));
    }
}