package com.ecommerce.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardPaymentDTO {
    @NotEmpty(message = "Card token is required")
    private String token;

    @NotNull(message = "Installments are required")
    private Integer installments;

    // Optional: not returned by all card types (e.g. debit)
    private String issuerId;

    @NotEmpty(message = "Payment method ID is required")
    private String paymentMethodId;

    @NotNull(message = "Payer information is required")
    @Valid
    private PayerDTO payer;

    @Data
    public static class PayerDTO {
        @NotEmpty(message = "Payer email is required")
        private String email;

        // Optional: not required for all payment flows
        @Valid
        private IdentificationDTO identification;
    }

    @Data
    public static class IdentificationDTO {
        private String type;
        private String number;
    }
}
