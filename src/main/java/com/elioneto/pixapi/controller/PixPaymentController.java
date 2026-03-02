package com.elioneto.pixapi.controller;

import com.elioneto.pixapi.dto.CreatePixRequest;
import com.elioneto.pixapi.dto.PixPaymentResponse;
import com.elioneto.pixapi.service.PixPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/pix/payments")
@RequiredArgsConstructor
public class PixPaymentController {

    private final PixPaymentService pixPaymentService;

    /**
     * POST /pix/payments
     * Inicia um pagamento Pix conforme padrão Open Finance Brasil (Fase 3)
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PixPaymentResponse> createPayment(
            @Valid @RequestBody CreatePixRequest request) {
        log.info("POST /pix/payments - pixKey={}, amount={}", request.getPixKey(), request.getAmount());
        PixPaymentResponse response = pixPaymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /pix/payments/{id}
     * Consulta o status de um pagamento Pix
     * Simula estados: PENDING → PROCESSING → COMPLETED/FAILED
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PixPaymentResponse> getPayment(@PathVariable UUID id) {
        log.info("GET /pix/payments/{}", id);
        return ResponseEntity.ok(pixPaymentService.getPayment(id));
    }
}
