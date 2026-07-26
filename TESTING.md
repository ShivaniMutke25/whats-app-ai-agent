# WhatsApp AI Agent Testing Guide

This document explains how to run the existing test suite, add tests, and validate the application in both local and production-like environments.

## Run All Tests

From the project root:

```bash
mvn test
```

This executes all JUnit tests under `src/test/java`.

## Run Specific Tests

Run a single test class:

```bash
mvn -Dtest=IdempotencyServiceTest test
```

Run multiple test classes by pattern:

```bash
mvn -Dtest=*ServiceTest test
```

## Common Test Commands

- `mvn test` - run the full test suite.
- `mvn -DskipTests package` - build without running tests.
- `mvn -Dtest=WebhookControllerTest test` - run only the webhook controller tests.
- `mvn -Dtest=InboundMessageFlowTest test` - run the inbound message normalization flow test.

## Existing Test Coverage

The project currently includes:

- `WhatsAppAiAgentApplicationTests` - Spring Boot context load test.
- `WebhookControllerTest` - webhook verification and inbound request handling.
- `IdempotencyServiceTest` - duplicate claim prevention.
- `RateLimitServiceTest` - rate limiting logic.
- `FallbackServiceTest` - safe fallback text generation.
- `WhatsAppResponseServiceTest` - outbound response service mock behavior.
- `InboundMessageFlowTest` - normalization flow from WhatsApp payload to `WhatsAppMessage`.

## Test Categories

### Unit Tests

These tests validate small, isolated classes.
Examples:
- `IdempotencyServiceTest`
- `RateLimitServiceTest`
- `FallbackServiceTest`

### Controller Tests

These tests use Spring `MockMvc` to simulate HTTP requests.
Examples:
- `WebhookControllerTest`

### Integration Flow Tests

These tests exercise a larger path through application components.
Examples:
- `InboundMessageFlowTest`

## Add a New Test

1. Create a new class under `src/test/java/com/example/whatsappai/...`.
2. Use JUnit 5 annotations (`@Test`).
3. Use `@SpringBootTest` only for tests requiring Spring context.
4. Use `@AutoConfigureMockMvc` for controller tests.
5. Name the test class with `Test` suffix.

### Example

```java
package com.example.whatsappai.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PromptServiceTest {

    @Test
    void buildPromptReturnsNonEmptyString() {
        PromptService service = new PromptService();
        String prompt = service.buildPrompt("Hello", List.of(), List.of(), "");

        assertNotNull(prompt);
        assertFalse(prompt.isBlank());
    }
}
```

## Testing with Kafka and Redis

The application relies on Kafka and Redis during runtime but most tests are designed to run without requiring those services.

For local integration testing with dependencies, start Docker Compose:

```bash
docker-compose up -d
```

Then run the application or tests as usual.

## Production Validation

To validate a production build with tests and application start:

```bash
mvn clean test package
```

Then run the jar with production environment variables:

```bash
OPENAI_API_KEY=your_key \
WHATSAPP_ACCESS_TOKEN=your_whatsapp_token \
WHATSAPP_PHONE_NUMBER_ID=your_phone_number_id \
KAFKA_BOOTSTRAP_SERVERS=broker:9092 \
REDIS_HOST=redis:6379 \
java -jar target/whatsapp-ai-agent-0.0.1-SNAPSHOT.jar
```

After the app starts, verify:

- `http://localhost:8080/actuator/health`
- `http://localhost:8080/whatsapp/webhook?...`

Send a real or mock webhook payload to `/whatsapp/webhook` and confirm the application accepts it.

## Debugging Test Failures

- Inspect the Maven output for stack traces.
- Run the failing test class directly with `-Dtest=`.
- Confirm environment variables are not interfering with local test execution.
- For Spring context failures, check that the component under test has the expected constructor and annotations.

## Notes

- The current test suite is small but covers core startup, webhook entry, and key service behavior.
- Future improvements may include more AI orchestration tests, Redis/ Kafka integration tests, and end-to-end WhatsApp payload processing scenarios.
