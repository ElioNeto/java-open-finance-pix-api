package com.elioneto.pixapi.dto;

import com.elioneto.pixapi.model.PixStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registro de mudança de status de uma transação Pix")
public class PixPaymentStatusLogResponse {

    @Schema(description = "Status registrado neste momento", example = "PROCESSING")
    private PixStatus status;

    @Schema(
            description = "Origem da mudança de status",
            example = "KAFKA_CONSUMER",
            allowableValues = {"TPP_INITIATION", "KAFKA_CONSUMER", "SPI_BANCO_CENTRAL", "WEBHOOK_CALLBACK"}
    )
    private String source;

    @Schema(description = "Data e hora em que o status foi registrado", example = "2026-03-03T12:00:00")
    private LocalDateTime changedAt;
}
