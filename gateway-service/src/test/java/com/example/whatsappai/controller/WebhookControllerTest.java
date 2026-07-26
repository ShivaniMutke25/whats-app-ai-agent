package com.example.whatsappai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void verifyWebhookReturnsChallenge() throws Exception {
        mockMvc.perform(get("/whatsapp/webhook")
                        .param("hub.mode", "subscribe")
                        .param("hub.challenge", "challenge-123")
                        .param("hub.verify_token", "local-verify-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("challenge-123"));
    }

    @Test
    void inboundWebhookAcceptsTextMessage() throws Exception {
        String payload = """
                {
                  "object": "whatsapp_business_account",
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "+123456789",
                              "phone_number_id": "phone-1"
                            },
                            "messages": [
                              {
                                "from": "919876543210",
                                "id": "msg-123",
                                "timestamp": "1710000000",
                                "type": "text",
                                "text": {
                                  "body": "Hello from customer"
                                }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/whatsapp/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("accepted"))
                .andExpect(jsonPath("$.messageId").value("msg-123"))
                .andExpect(jsonPath("$.correlationId").exists())
                .andExpect(jsonPath("$.customerId").value("919876543210"));
    }

    @Test
    void invalidWebhookPayloadReturnsBadRequest() throws Exception {
        String payload = """
                {
                  "object": "whatsapp_business_account",
                  "entry": []
                }
                """;

        mockMvc.perform(post("/whatsapp/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("entry")));
    }
}
