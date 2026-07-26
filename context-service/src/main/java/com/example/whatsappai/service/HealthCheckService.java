package com.example.whatsappai.service;

import org.springframework.stereotype.Service;

@Service
public class HealthCheckService {

    public String getStatus() {
        return "UP";
    }
}
