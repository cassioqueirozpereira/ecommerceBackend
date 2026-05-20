package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.NewsletterRequestDTO;
import com.ecommerce.backend.dto.NewsletterResponseDTO;
import com.ecommerce.backend.service.NewsletterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterService newsletterService;

    @PostMapping("/subscribe")
    public ResponseEntity<NewsletterResponseDTO> subscribe(@Valid @RequestBody NewsletterRequestDTO request) {
        return ResponseEntity.ok(newsletterService.subscribe(request));
    }
}
