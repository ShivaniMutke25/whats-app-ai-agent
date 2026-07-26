package com.example.whatsappai.guardrail;

import org.springframework.stereotype.Service;

@Service
public class InputGuardrail {

    public boolean isValid(String message) {
        return message != null && !message.isBlank();
    }
}
