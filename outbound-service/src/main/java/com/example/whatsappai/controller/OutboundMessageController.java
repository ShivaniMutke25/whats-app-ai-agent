package com.example.whatsappai.controller;

import com.example.whatsappai.service.WhatsAppResponseService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/messages")
public class OutboundMessageController {
    private final WhatsAppResponseService responseService;
    public OutboundMessageController(WhatsAppResponseService responseService) { this.responseService = responseService; }
    @PostMapping
    public ResponseEntity<Void> send(@RequestBody OutboundMessage request) {
        responseService.sendReply(request.to(), request.message());
        return ResponseEntity.accepted().build();
    }
    public record OutboundMessage(@NotBlank String to, @NotBlank String message) { }
}
