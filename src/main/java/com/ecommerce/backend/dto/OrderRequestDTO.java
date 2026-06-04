package com.ecommerce.backend.dto;

import com.ecommerce.backend.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequestDTO> items;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    // --- Pix Manual fields (required only when paymentMethod = PIX_MANUAL) ---
    private String payerName;
    private String payerCpf;
    private String payerPhone;
    private String pixReceiptUrl;
}
