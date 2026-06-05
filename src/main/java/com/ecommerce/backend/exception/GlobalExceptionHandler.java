package com.ecommerce.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.ecommerce.backend.exception.PaymentRejectedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Bad Request");
        problemDetail.setType(URI.create("https://ecommerce.com/errors/bad-request"));
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problemDetail.setTitle("Validation Error");
        problemDetail.setType(URI.create("https://ecommerce.com/errors/validation"));

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        problemDetail.setProperty("invalid_params", errors);
        return problemDetail;
    }

    @ExceptionHandler(PaymentRejectedException.class)
    public ProblemDetail handlePaymentRejectedException(PaymentRejectedException ex) {
        String translatedMessage = switch (ex.getCode() != null ? ex.getCode() : "") {
            case "cc_rejected_bad_filled_cvv", "cc_rejected_bad_filled_security_code" -> "Código de segurança inválido.";
            case "cc_rejected_bad_filled_date" -> "Data de validade inválida.";
            case "cc_rejected_bad_filled_card_number" -> "Número do cartão inválido.";
            case "cc_rejected_bad_filled_other" -> "Dados do cartão inválidos.";
            case "cc_rejected_insufficient_amount" -> "Saldo insuficiente.";
            case "cc_rejected_call_for_authorize" -> "Cartão não autorizado. Entre em contato com seu banco.";
            case "cc_rejected_high_risk" -> "Transação recusada por segurança.";
            case "cc_rejected_blacklist" -> "Pagamento não autorizado.";
            case "cc_rejected_max_attempts" -> "Número máximo de tentativas atingido.";
            case "cc_rejected_card_disabled" -> "Cartão desativado. Entre em contato com seu banco.";
            case "cc_rejected_duplicated_payment" -> "Pagamento duplicado.";
            case "api_E301", "api_316" -> "Número do cartão inválido.";
            case "api_E302", "api_224" -> "Código de segurança inválido.";
            case "api_325", "api_326", "api_208", "api_209" -> "Data de validade inválida.";
            case "api_324", "api_322", "api_323", "api_212", "api_213", "api_214" -> "Documento do titular inválido.";
            case "api_221" -> "Nome do titular inválido.";
            case "api_205" -> "Digite o número do cartão.";
            default -> "Não foi possível processar o pagamento. (Erro: " + (ex.getCode() != null ? ex.getCode() : "desconhecido") + ")";
        };

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, translatedMessage);
        problemDetail.setTitle("Payment Rejected");
        problemDetail.setType(URI.create("https://ecommerce.com/errors/payment-rejected"));
        problemDetail.setProperty("code", ex.getCode());
        problemDetail.setProperty("message", translatedMessage);
        
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAllOtherExceptions(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("https://ecommerce.com/errors/internal-server-error"));
        // Don't expose internal exception message directly in production
        return problemDetail;
    }
}
