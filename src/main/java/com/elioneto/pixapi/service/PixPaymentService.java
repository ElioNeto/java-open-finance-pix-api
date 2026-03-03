package com.elioneto.pixapi.service;

import com.elioneto.pixapi.dto.CreatePixRequest;
import com.elioneto.pixapi.dto.PixPaymentResponse;
import com.elioneto.pixapi.dto.PixPaymentStatusLogResponse;
import com.elioneto.pixapi.dto.PixPaymentSummaryResponse;
import com.elioneto.pixapi.dto.WebhookRequest;
import com.elioneto.pixapi.exception.PixPaymentNotFoundException;
import com.elioneto.pixapi.kafka.PixEvent;
import com.elioneto.pixapi.kafka.PixPaymentProducer;
import com.elioneto.pixapi.model.PixPayment;
import com.elioneto.pixapi.model.PixPaymentStatusLog;
import com.elioneto.pixapi.model.PixStatus;
import com.elioneto.pixapi.repository.PixPaymentRepository;
import com.elioneto.pixapi.repository.PixPaymentStatusLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PixPaymentService {

    private final PixPaymentRepository pixPaymentRepository;
    private final PixPaymentProducer pixPaymentProducer;
    private final PixPaymentStatusLogRepository statusLogRepository;

    @Transactional
    public PixPaymentResponse createPayment(CreatePixRequest request) {
        log.info("Creating Pix payment: pixKey={}, amount={}", request.getPixKey(), request.getAmount());

        PixPayment payment = PixPayment.builder()
                .pixKey(request.getPixKey())
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(PixStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        payment = pixPaymentRepository.save(payment);

        saveStatusLog(payment.getId(), PixStatus.PENDING, "TPP_INITIATION");

        PixEvent event = PixEvent.builder()
                .paymentId(payment.getId())
                .pixKey(payment.getPixKey())
                .amount(payment.getAmount())
                .description(payment.getDescription())
                .build();

        pixPaymentProducer.sendPixEvent(event);

        log.info("Pix payment created and Kafka event published: id={}", payment.getId());
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PixPaymentSummaryResponse> listPayments(PixStatus status) {
        log.info("Listing Pix payments, status filter={}", status);

        List<PixPayment> payments = (status != null)
                ? pixPaymentRepository.findByStatusOrderByCreatedAtDesc(status)
                : pixPaymentRepository.findAllByOrderByCreatedAtDesc();

        log.info("Found {} payments", payments.size());
        return payments.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PixPaymentResponse getPayment(UUID id) {
        log.info("Fetching Pix payment: id={}", id);
        PixPayment payment = pixPaymentRepository.findById(id)
                .orElseThrow(() -> new PixPaymentNotFoundException("Pagamento não encontrado: " + id));
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PixPaymentStatusLogResponse> getStatusLogs(UUID id) {
        log.info("Fetching status logs for paymentId={}", id);

        if (!pixPaymentRepository.existsById(id)) {
            throw new PixPaymentNotFoundException("Pagamento não encontrado: " + id);
        }

        return statusLogRepository.findByPaymentIdOrderByChangedAtAsc(id)
                .stream()
                .map(log -> PixPaymentStatusLogResponse.builder()
                        .status(log.getStatus())
                        .source(log.getSource())
                        .changedAt(log.getChangedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public PixPaymentResponse processWebhook(WebhookRequest request) {
        log.info("Processing webhook for paymentId={}, status={}",
                request.getPaymentId(), request.getStatus());

        PixPayment payment = pixPaymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new PixPaymentNotFoundException(
                        "Pagamento não encontrado: " + request.getPaymentId()));

        payment.setStatus(request.getStatus());
        payment.setUpdatedAt(LocalDateTime.now());
        payment = pixPaymentRepository.save(payment);

        saveStatusLog(payment.getId(), request.getStatus(), "WEBHOOK_CALLBACK");

        log.info("Payment status updated via webhook: id={}, status={}",
                payment.getId(), payment.getStatus());
        return toResponse(payment);
    }

    public void saveStatusLog(UUID paymentId, PixStatus status, String source) {
        PixPaymentStatusLog logEntry = PixPaymentStatusLog.builder()
                .paymentId(paymentId)
                .status(status)
                .source(source)
                .changedAt(LocalDateTime.now())
                .build();
        statusLogRepository.save(logEntry);
        log.info("[STATUS LOG] paymentId={} status={} source={}", paymentId, status, source);
    }

    private PixPaymentSummaryResponse toSummaryResponse(PixPayment payment) {
        return PixPaymentSummaryResponse.builder()
                .id(payment.getId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .pixKey(payment.getPixKey())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private PixPaymentResponse toResponse(PixPayment payment) {
        return PixPaymentResponse.builder()
                .id(payment.getId())
                .pixKey(payment.getPixKey())
                .amount(payment.getAmount())
                .description(payment.getDescription())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
