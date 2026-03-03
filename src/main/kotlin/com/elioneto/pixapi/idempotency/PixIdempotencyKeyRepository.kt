package com.elioneto.pixapi.idempotency

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface PixIdempotencyKeyRepository : JpaRepository<PixIdempotencyKey, UUID> {
    fun findByIdempotencyKey(idempotencyKey: String): Optional<PixIdempotencyKey>
}
