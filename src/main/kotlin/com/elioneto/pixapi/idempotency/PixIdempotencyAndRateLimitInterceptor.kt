package com.elioneto.pixapi.idempotency

import com.elioneto.pixapi.model.PixPayment
import com.elioneto.pixapi.repository.PixPaymentRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.HandlerInterceptor
import java.io.IOException
import java.time.Instant
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class PixIdempotencyAndRateLimitInterceptor(
    private val idempotencyRepository: PixIdempotencyKeyRepository,
    private val pixPaymentRepository: PixPaymentRepository,
) : HandlerInterceptor {

    private val log = LoggerFactory.getLogger(javaClass)

    private val requestsPerUser: MutableMap<String, MutableList<Instant>> = ConcurrentHashMap()

    private val windowSeconds: Long = 60
    private val maxRequestsPerWindow: Int = 60

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (request.method != HttpMethod.POST.name || !request.requestURI.startsWith("/pix/payments")) {
            return true
        }

        val user = request.userPrincipal?.name ?: "anonymous"

        if (!applyRateLimiting(user, response)) {
            return false
        }

        val key = request.getHeader("Idempotency-Key") ?: request.getParameter("txId")

        if (key.isNullOrBlank()) {
            return true
        }

        val existing = idempotencyRepository.findByIdempotencyKey(key)
        if (existing.isPresent) {
            val id = existing.get().paymentId
            log.info("[IDEMPOTENCY] Requisição repetida detectada. key={} paymentId={}", key, id)

            val paymentOpt = pixPaymentRepository.findById(id)
            if (paymentOpt.isEmpty) {
                response.status = HttpStatus.NOT_FOUND.value()
                response.writer.write("{\"error\":\"Pagamento associado à Idempotency-Key não encontrado\"}")
                return false
            }

            val payment = paymentOpt.get()
            writePixPaymentResponse(response, payment)
            return false
        }

        request.setAttribute("IDEMPOTENCY_KEY", key)
        return true
    }

    @Transactional
    fun registerKeyIfNeeded(idempotencyKey: String?, payment: PixPayment) {
        if (idempotencyKey.isNullOrBlank()) return
        if (idempotencyRepository.findByIdempotencyKey(idempotencyKey).isPresent) return

        val entity = PixIdempotencyKey(
            id = UUID.randomUUID(),
            idempotencyKey = idempotencyKey,
            paymentId = payment.id,
            status = IdempotencyStatus.COMPLETED,
        )
        idempotencyRepository.save(entity)
        log.info("[IDEMPOTENCY] Idempotency-Key registrada. key={} paymentId={}", idempotencyKey, payment.id)
    }

    private fun applyRateLimiting(username: String, response: HttpServletResponse): Boolean {
        val now = Instant.now()
        val windowStart = now.minusSeconds(windowSeconds)

        val timestamps = requestsPerUser.computeIfAbsent(username) { mutableListOf() }

        timestamps.removeIf { it.isBefore(windowStart) }

        if (timestamps.size >= maxRequestsPerWindow) {
            log.warn("[RATE-LIMIT] Usuario {} excedeu o limite de {} req/{}s", username, maxRequestsPerWindow, windowSeconds)
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.writer.write("{\"error\":\"Too Many Requests\"}")
            return false
        }

        timestamps.add(now)
        return true
    }

    @Throws(IOException::class)
    private fun writePixPaymentResponse(response: HttpServletResponse, payment: PixPayment) {
        response.status = HttpStatus.OK.value()
        response.contentType = "application/json"
        val json = """
            {
              \"id\": \"${payment.id}\",
              \"pixKey\": \"${payment.pixKey}\",
              \"amount\": ${payment.amount},
              \"description\": ${if (payment.description != null) "\"${payment.description}\"" else null},
              \"status\": \"${payment.status}\",
              \"createdAt\": \"${payment.createdAt}\",
              \"updatedAt\": ${if (payment.updatedAt != null) "\"${payment.updatedAt}\"" else null}
            }
        """.trimIndent()
        response.writer.write(json)
    }
}
