package com.ecommerce.backend.controller;

import com.cloudinary.Cloudinary;
import com.ecommerce.backend.dto.SignatureResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @GetMapping("/signature")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SignatureResponse> getSignature(@RequestParam(defaultValue = "ecommerce/products") String folder) {
        long timestamp = System.currentTimeMillis() / 1000L;
        
        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", timestamp);
        params.put("folder", folder);

        String signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret);

        SignatureResponse response = SignatureResponse.builder()
                .signature(signature)
                .timestamp(timestamp)
                .cloudName(cloudName)
                .apiKey(apiKey)
                .folder(folder)
                .build();

        return ResponseEntity.ok(response);
    }
}
