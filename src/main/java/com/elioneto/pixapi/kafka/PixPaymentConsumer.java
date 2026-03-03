package com.elioneto.pixapi.kafka;

import com.elioneto.pixapi.model.PixPaymentStatusLog;
import com.elioneto.pixapi.model.PixStatus;
import com.elioneto.pixapi.repository.PixPaymentRepository;
import com.elioneto.pixapi.repository.PixPaymentStatusLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class PixPaymentConsumer {

    private final PixPaymentRepository pixPaymentRepository;
    private final PixPaymentStatusLogRepository statusLogRepository;
    private final Random random = new Random();

    @KafkaListener(topics = "pix-payments", groupId = "pix-group")
    public void processPayment(PixEvent event) {
        log.info("[KAFKA] Processing Pix event: paymentId={}", event.getPaymentId());

        sleepSeconds(2); // Simula envio ao SPI do Banco Central

        // Atualiza para PROCESSING
        pixPaymentRepository.findById(event.getPaymentId()).ifPresent(payment -> {
            payment.setStatus(PixStatus.PROCESSING);
            payment.setUpdatedAt(LocalDateTime.now());
            pixPaymentRepository.save(payment);
            saveStatusLog(payment.getId(), PixStatus.PROCESSING, "KAFKA_CONSUMER");
            log.info("[KAFKA] Payment PROCESSING: id={}", payment.getId());
        });

        sleepSeconds(3); // Simula validação pelo SPI do Banco Central

        // Atualiza para COMPLETED ou FAILED (90% sucesso, simula padrão real do SPI)
        pixPaymentRepository.findById(event.getPaymentId()).ifPresent(payment -> {
            PixStatus finalStatus = random.nextInt(10) < 9 ? PixStatus.COMPLETED : PixStatus.FAILED;
            payment.setStatus(finalStatus);
            payment.setUpdatedAt(LocalDateTime.now());
            pixPaymentRepository.save(payment);
            saveStatusLog(payment.getId(), finalStatus, "SPI_BANCO_CENTRAL");
            log.info("[KAFKA] Payment finalized: id={}, status={}", payment.getId(), finalStatus);
        });
    }

    private void saveStatusLog(java.util.UUID paymentId, PixStatus status, String source) {
        PixPaymentStatusLog logEntry = PixPaymentStatusLog.builder()
                .paymentId(paymentId)
                .status(status)
                .source(source)
                .changedAt(LocalDateTime.now())
                .build();
        statusLogRepository.save(logEntry);
    }

    private void sleepSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
