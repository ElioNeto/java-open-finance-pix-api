package com.elioneto.pixapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pix_payment_status_logs", indexes = {
        @Index(name = "idx_status_log_payment_id", columnList = "paymentId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PixPaymentStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PixStatus status;

    /**
     * Origem da mudança de status:
     * TPP_INITIATION     - Criado pelo TPP via REST
     * KAFKA_CONSUMER     - Processado pelo consumer Kafka (envio ao SPI)
     * SPI_BANCO_CENTRAL  - Resposta final do SPI do Banco Central
     * WEBHOOK_CALLBACK   - Atualizado via webhook externo
     */
    @Column(nullable = false, length = 50)
    private String source;

    @Column(nullable = false)
    private LocalDateTime changedAt;
}
