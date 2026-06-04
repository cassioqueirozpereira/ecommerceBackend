package com.ecommerce.backend.dto;

import com.ecommerce.backend.entity.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class OrderResponseDTO {
    private UUID id;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal total;
    private PaymentMethod paymentMethod;
    private String pixReceiptUrl;
    private String payerName;
    private LocalDateTime createdAt;
    private List<OrderItemResponseDTO> items;
}
