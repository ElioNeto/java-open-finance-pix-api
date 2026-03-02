package com.elioneto.pixapi.dto;

import com.elioneto.pixapi.model.PixStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class WebhookRequest {

    @NotNull(message = "paymentId é obrigatório")
    private UUID paymentId;

    @NotNull(message = "status é obrigatório")
    private PixStatus status;

    private String message;
}
