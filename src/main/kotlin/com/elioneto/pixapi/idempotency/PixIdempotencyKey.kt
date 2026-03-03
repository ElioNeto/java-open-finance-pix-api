package com.elioneto.pixapi.idempotency

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "pix_idempotency_keys")
class PixIdempotencyKey(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true, length = 100)
    val idempotencyKey: String,

    @Column(nullable = false)
    val paymentId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: IdempotencyStatus = IdempotencyStatus.COMPLETED,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

enum class IdempotencyStatus {
    PENDING,
    COMPLETED
}
