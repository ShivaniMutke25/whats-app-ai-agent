package com.example.whatsappai.kafka;

import com.example.whatsappai.dto.whatsapp.IncomingMessage;
import com.example.whatsappai.dto.whatsapp.MessageText;
import com.example.whatsappai.dto.whatsapp.WebhookChange;
import com.example.whatsappai.dto.whatsapp.WebhookEntry;
import com.example.whatsappai.dto.whatsapp.WebhookMetadata;
import com.example.whatsappai.dto.whatsapp.WebhookRequest;
import com.example.whatsappai.dto.whatsapp.WebhookValue;
import com.example.whatsappai.service.InboundMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class InboundMessageFlowTest {

    @Autowired
    private InboundMessageService inboundMessageService;

    @Test
    void normalizeWebhookRequestProducesMessage() {
        WebhookRequest request = new WebhookRequest(
                "whatsapp_business_account",
                List.of(new WebhookEntry(List.of(new WebhookChange(
                        new WebhookValue(
                                new WebhookMetadata("+123456789", "phone-1"),
                                List.of(new IncomingMessage("919876543210", "msg-456", 1710000000L, "text", new MessageText("Hello")))
                        ))
                ))));

        assertDoesNotThrow(() -> inboundMessageService.normalize(request));
    }
}
