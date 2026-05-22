package com.ecommerce.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignatureResponse {
    private String signature;
    private long timestamp;
    private String cloudName;
    private String apiKey;
    private String folder;
}
