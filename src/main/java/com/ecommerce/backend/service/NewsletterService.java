package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.NewsletterRequestDTO;
import com.ecommerce.backend.dto.NewsletterResponseDTO;
import com.ecommerce.backend.entity.Newsletter;
import com.ecommerce.backend.repository.NewsletterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsletterService {

    private final NewsletterRepository newsletterRepository;

    public NewsletterResponseDTO subscribe(NewsletterRequestDTO request) {
        String email = request.getEmail().toLowerCase().trim();

        if (newsletterRepository.existsByEmail(email)) {
            // Idempotent: se já existe, retorna sucesso amigável (sem erro 500)
            return new NewsletterResponseDTO("E-mail já inscrito.");
        }

        Newsletter newsletter = Newsletter.builder()
                .email(email)
                .build();
        
        newsletterRepository.save(newsletter);
        
        return new NewsletterResponseDTO("Inscrição realizada com sucesso!");
    }
}
