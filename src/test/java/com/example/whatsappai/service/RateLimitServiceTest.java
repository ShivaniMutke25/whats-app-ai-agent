package com.example.whatsappai.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitServiceTest {

    @Test
    void exceedsAllowedRequestsAfterLimitIsReached() {
        RateLimitService service = new RateLimitService(2, 1000);

        assertTrue(service.allow("cust-1"));
        assertTrue(service.allow("cust-1"));
        assertFalse(service.allow("cust-1"));
    }
}
