package com.ecommerce.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtAuthenticationResponseDTO {
    private String accessToken;
    private String tokenType = "Bearer";

    public JwtAuthenticationResponseDTO(String accessToken) {
        this.accessToken = accessToken;
    }
}
