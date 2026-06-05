package com.ecommerce.backend.exception;

public class PaymentRejectedException extends RuntimeException {
    
    private final String code;
    
    public PaymentRejectedException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}
