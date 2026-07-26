package com.example.whatsappai.kafka.producer;

import com.example.whatsappai.kafka.WhatsAppMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InboundMessageProducer {

    private static final Logger log = LoggerFactory.getLogger(InboundMessageProducer.class);

    private final KafkaTemplate<String, WhatsAppMessageEvent> kafkaTemplate;

    @Value("${app.kafka.topic.inbound:whatsapp.inbound}")
    private String inboundTopic;

    public InboundMessageProducer(KafkaTemplate<String, WhatsAppMessageEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(WhatsAppMessageEvent event) {
        try {
            kafkaTemplate.send(inboundTopic, event.messageId(), event)
                    .thenAccept(result -> log.info("Published inbound WhatsApp event messageId={} partition={} offset={}",
                            event.messageId(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset()))
                    .exceptionally(ex -> {
                        log.warn("Kafka publish failed for messageId={} topic={} error={}", event.messageId(), inboundTopic, ex.getMessage());
                        return null;
                    });
        } catch (RuntimeException ex) {
            log.warn("Kafka publish failed for messageId={} topic={} error={}", event.messageId(), inboundTopic, ex.getMessage());
        }
    }
}
