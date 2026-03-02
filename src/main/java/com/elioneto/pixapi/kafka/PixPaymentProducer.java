package com.elioneto.pixapi.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PixPaymentProducer {

    private static final String TOPIC = "pix-payments";

    private final KafkaTemplate<String, PixEvent> kafkaTemplate;

    public void sendPixEvent(PixEvent event) {
        log.info("Publishing Pix event to Kafka: paymentId={}, pixKey={}",
                event.getPaymentId(), event.getPixKey());
        kafkaTemplate.send(TOPIC, event.getPaymentId().toString(), event);
    }
}
