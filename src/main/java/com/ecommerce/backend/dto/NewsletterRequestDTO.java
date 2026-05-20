package com.ecommerce.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewsletterRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;
}
