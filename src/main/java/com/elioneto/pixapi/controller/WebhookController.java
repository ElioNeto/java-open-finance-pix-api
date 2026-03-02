package com.elioneto.pixapi.controller;

import com.elioneto.pixapi.dto.PixPaymentResponse;
import com.elioneto.pixapi.dto.WebhookRequest;
import com.elioneto.pixapi.service.PixPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final PixPaymentService pixPaymentService;

    /**
     * POST /webhooks/pix-callback
     * Simula callback do Banco Central atualizando status do pagamento
     * Em produção, este endpoint seria chamado pelo SPI após processar o Pix
     */
    @PostMapping("/pix-callback")
    public ResponseEntity<PixPaymentResponse> pixCallback(
            @Valid @RequestBody WebhookRequest request) {
        log.info("Webhook received from BC: paymentId={}, status={}",
                request.getPaymentId(), request.getStatus());
        return ResponseEntity.ok(pixPaymentService.processWebhook(request));
    }
}
