package com.example.whatsappai.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EvaluationServiceTest {

    @Test
    void evaluationRunnerProducesResults() {
        EvaluationService service = new EvaluationService();
        var results = service.run();

        assertNotNull(results);
        assertFalse(results.isEmpty());
    }
}
