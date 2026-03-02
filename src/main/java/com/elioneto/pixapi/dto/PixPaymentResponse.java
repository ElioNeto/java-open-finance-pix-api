package com.elioneto.pixapi.dto;

import com.elioneto.pixapi.model.PixStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PixPaymentResponse {
    private UUID id;
    private String pixKey;
    private BigDecimal amount;
    private String description;
    private PixStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
