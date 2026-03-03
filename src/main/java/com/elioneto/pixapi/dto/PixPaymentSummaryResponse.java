package com.elioneto.pixapi.dto;

import com.elioneto.pixapi.model.PixStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumo de uma transação Pix")
public class PixPaymentSummaryResponse {

    @Schema(description = "ID único da transação", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Status atual da transação", example = "PENDING")
    private PixStatus status;

    @Schema(description = "Valor da transação em reais", example = "150.00")
    private BigDecimal amount;

    @Schema(description = "Chave Pix do destinatário", example = "joao@email.com")
    private String pixKey;

    @Schema(description = "Data e hora de criação")
    private LocalDateTime createdAt;
}
