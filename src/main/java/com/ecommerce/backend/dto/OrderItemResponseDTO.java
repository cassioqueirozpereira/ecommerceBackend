package com.ecommerce.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OrderItemResponseDTO {
    private UUID id;
    private ProductDTO product;
    private VariantDTO variant;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
