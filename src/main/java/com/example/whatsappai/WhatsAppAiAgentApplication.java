package com.example.whatsappai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WhatsAppAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(WhatsAppAiAgentApplication.class, args);
    }
}
