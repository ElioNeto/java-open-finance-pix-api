package com.elioneto.pixapi.idempotency

import com.fasterxml.jackson.databind.ObjectMapper
import com.elioneto.pixapi.repository.PixPaymentRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class PixIdempotencyAndRateLimitInterceptor(
    private val idempotencyRepository: PixIdempotencyKeyRepository,
    private val pixPaymentRepository: PixPaymentRepository,
    private val objectMapper: ObjectMapper,
) : HandlerInterceptor {

    private val log = LoggerFactory.getLogger(javaClass)

    private val requestsPerUser: MutableMap<String, MutableList<Instant>> = ConcurrentHashMap()
    private val windowSeconds: Long = 60
    private val maxRequestsPerWindow: Int = 60

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // Somente intercepta POST /pix/payments
        if (request.method != "POST" || !request.requestURI.startsWith("/pix/payments")) {
            return true
        }

        val user = request.userPrincipal?.name ?: "anonymous"

        // 1) Rate limiting
        if (!applyRateLimiting(user, response)) return false

        // 2) Idempotência
        val key = request.getHeader("Idempotency-Key") ?: request.getParameter("txId")
        if (key.isNullOrBlank()) return true

        val existing = idempotencyRepository.findByIdempotencyKey(key)
        if (existing.isPresent) {
            val paymentId = existing.get().paymentId
            log.info("[IDEMPOTENCY] Requisição repetida detectada. key={} paymentId={}", key, paymentId)

            val paymentOpt = pixPaymentRepository.findById(paymentId)
            if (paymentOpt.isEmpty) {
                response.status = HttpStatus.NOT_FOUND.value()
                response.contentType = "application/json"
                response.writer.write("""{"error":"Pagamento associado à Idempotency-Key não encontrado"}""")
                return false
            }

            // Serializa via Jackson — sem acessar campos Lombok diretamente
            writeJsonResponse(response, HttpStatus.OK, paymentOpt.get())
            return false
        }

        // Chave não existe ainda: armazena no request para o service registrar após criar o pagamento
        request.setAttribute("IDEMPOTENCY_KEY", key)
        return true
    }

    /**
     * Registra a Idempotency-Key após o pagamento ser criado com sucesso.
     * Chamado pelo PixPaymentService com o UUID já gerado.
     * Recebe UUID diretamente para não acessar campos Lombok de PixPayment durante a compilação Kotlin.
     */
    @Transactional
    fun registerKeyIfNeeded(idempotencyKey: String?, paymentId: UUID) {
        if (idempotencyKey.isNullOrBlank()) return
        if (idempotencyRepository.findByIdempotencyKey(idempotencyKey).isPresent) return

        val entity = PixIdempotencyKey(
            idempotencyKey = idempotencyKey,
            paymentId = paymentId,
        )
        idempotencyRepository.save(entity)
        log.info("[IDEMPOTENCY] Idempotency-Key registrada. key={} paymentId={}", idempotencyKey, paymentId)
    }

    private fun applyRateLimiting(username: String, response: HttpServletResponse): Boolean {
        val now = Instant.now()
        val windowStart = now.minusSeconds(windowSeconds)
        val timestamps = requestsPerUser.computeIfAbsent(username) { mutableListOf() }

        timestamps.removeIf { it.isBefore(windowStart) }

        if (timestamps.size >= maxRequestsPerWindow) {
            log.warn("[RATE-LIMIT] Usuario {} excedeu {} req/{}s", username, maxRequestsPerWindow, windowSeconds)
            writeJsonResponse(response, HttpStatus.TOO_MANY_REQUESTS, mapOf("error" to "Too Many Requests"))
            return false
        }

        timestamps.add(now)
        return true
    }

    private fun writeJsonResponse(response: HttpServletResponse, status: HttpStatus, body: Any) {
        response.status = status.value()
        response.contentType = "application/json"
        response.writer.write(objectMapper.writeValueAsString(body))
    }
}
