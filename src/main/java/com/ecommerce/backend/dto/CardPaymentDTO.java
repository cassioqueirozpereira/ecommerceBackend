package com.ecommerce.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardPaymentDTO {
    @NotEmpty(message = "Card token is required")
    private String token;
    
    @NotNull(message = "Installments are required")
    private Integer installments;
    
    @NotEmpty(message = "Issuer ID is required")
    private String issuerId;
    
    @NotEmpty(message = "Payment method ID is required")
    private String paymentMethodId;
    
    @NotNull(message = "Payer information is required")
    private PayerDTO payer;
    
    @Data
    public static class PayerDTO {
        @NotEmpty(message = "Payer email is required")
        private String email;
        
        @NotNull(message = "Payer identification is required")
        private IdentificationDTO identification;
    }
    
    @Data
    public static class IdentificationDTO {
        @NotEmpty(message = "Identification type is required")
        private String type;
        
        @NotEmpty(message = "Identification number is required")
        private String number;
    }
}
