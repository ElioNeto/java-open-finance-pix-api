package com.elioneto.pixapi.controller;

import com.elioneto.pixapi.dto.CreatePixRequest;
import com.elioneto.pixapi.dto.PixPaymentResponse;
import com.elioneto.pixapi.dto.PixPaymentSummaryResponse;
import com.elioneto.pixapi.model.PixStatus;
import com.elioneto.pixapi.service.PixPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/pix/payments")
@RequiredArgsConstructor
@Tag(name = "Pix Payments", description = "Endpoints de iniciação e consulta de pagamentos Pix — Open Finance Brasil Fase 3")
public class PixPaymentController {

    private final PixPaymentService pixPaymentService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Iniciar pagamento Pix",
            description = "Cria uma nova transação Pix e publica evento no Kafka para processamento assíncrono pelo SPI do Banco Central."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pagamento criado com status PENDING"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos — chave Pix vazia ou valor negativo"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido")
    })
    public ResponseEntity<PixPaymentResponse> createPayment(
            @Valid @RequestBody CreatePixRequest request) {
        log.info("POST /pix/payments - pixKey={}, amount={}", request.getPixKey(), request.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(pixPaymentService.createPayment(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Listar transações Pix",
            description = "Retorna todas as transações Pix ordenadas da mais recente para a mais antiga. " +
                    "Use o parâmetro `status` para filtrar: PENDING, PROCESSING, COMPLETED ou FAILED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido")
    })
    public ResponseEntity<List<PixPaymentSummaryResponse>> listPayments(
            @Parameter(description = "Filtrar por status (opcional). Valores: PENDING | PROCESSING | COMPLETED | FAILED")
            @RequestParam(required = false) PixStatus status) {
        log.info("GET /pix/payments - status filter={}", status);
        return ResponseEntity.ok(pixPaymentService.listPayments(status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Consultar pagamento Pix por ID",
            description = "Retorna os detalhes completos de uma transação. " +
                    "Estados possíveis: PENDING → PROCESSING → COMPLETED / FAILED"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido")
    })
    public ResponseEntity<PixPaymentResponse> getPayment(
            @Parameter(description = "UUID da transação Pix") @PathVariable UUID id) {
        log.info("GET /pix/payments/{}", id);
        return ResponseEntity.ok(pixPaymentService.getPayment(id));
    }
}
