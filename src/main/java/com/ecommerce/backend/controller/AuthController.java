package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.JwtAuthenticationResponseDTO;
import com.ecommerce.backend.dto.LoginRequestDTO;
import com.ecommerce.backend.dto.RegisterRequestDTO;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponseDTO> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        String token = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(new JwtAuthenticationResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        User result = authService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}