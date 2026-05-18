package com.ecommerce.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class OrderRequestDTO {
    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItemRequestDTO> items;

    @NotNull(message = "Payment method is required")
    private String paymentMethod; // e.g., "PIX", "CREDIT_CARD", "BOLETO"
}
