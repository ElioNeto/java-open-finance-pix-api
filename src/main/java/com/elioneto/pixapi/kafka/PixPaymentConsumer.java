package com.elioneto.pixapi.kafka;

import com.elioneto.pixapi.model.PixStatus;
import com.elioneto.pixapi.repository.PixPaymentRepository;
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
    private final Random random = new Random();

    @KafkaListener(topics = "pix-payments", groupId = "pix-group")
    public void processPayment(PixEvent event) {
        log.info("[KAFKA] Processing Pix event: paymentId={}", event.getPaymentId());

        sleepSeconds(2); // Simula envio ao SPI do Banco Central

        pixPaymentRepository.findById(event.getPaymentId()).ifPresent(payment -> {
            payment.setStatus(PixStatus.PROCESSING);
            payment.setUpdatedAt(LocalDateTime.now());
            pixPaymentRepository.save(payment);
            log.info("[KAFKA] Payment PROCESSING: id={}", payment.getId());
        });

        sleepSeconds(3); // Simula validação pelo Banco Central

        pixPaymentRepository.findById(event.getPaymentId()).ifPresent(payment -> {
            // Simula 90% de sucesso (padrão real do SPI)
            PixStatus finalStatus = random.nextInt(10) < 9 ? PixStatus.COMPLETED : PixStatus.FAILED;
            payment.setStatus(finalStatus);
            payment.setUpdatedAt(LocalDateTime.now());
            pixPaymentRepository.save(payment);
            log.info("[KAFKA] Payment finalized: id={}, status={}", payment.getId(), finalStatus);
        });
    }

    private void sleepSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
