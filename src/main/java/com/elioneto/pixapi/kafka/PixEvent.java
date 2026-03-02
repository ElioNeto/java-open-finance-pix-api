package com.elioneto.pixapi.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PixEvent {
    private UUID paymentId;
    private String pixKey;
    private BigDecimal amount;
    private String description;
}
