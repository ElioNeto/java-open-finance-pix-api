package com.elioneto.pixapi.controller;

import com.elioneto.pixapi.dto.LoginRequest;
import com.elioneto.pixapi.dto.TokenResponse;
import com.elioneto.pixapi.security.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * POST /auth/login
     * Autentica um TPP (Third Party Provider) e retorna JWT
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for TPP user: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = jwtUtils.generateToken(authentication.getName());

        log.info("JWT token generated for user: {}", request.getUsername());
        return ResponseEntity.ok(TokenResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400)
                .build());
    }
}
