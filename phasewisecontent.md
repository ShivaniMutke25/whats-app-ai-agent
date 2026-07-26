# WhatsApp AI Agent — Phased Implementation Plan

This document breaks the project into clear implementation phases so the work can be built incrementally and explained cleanly.

---

## Phase 1 — Project Skeleton and Dependencies

### Goal
Create the initial Spring Boot application structure with the core dependencies required for the prototype.

### What to build
- Spring Boot 3.x application
- Java 21 project setup
- Maven build configuration
- Basic package structure
- Health endpoint
- Basic configuration file

### Dependencies
- Spring Web
- Spring Validation
- Spring Boot Actuator
- Spring Boot Test
- Optional starter dependencies for future integration

### Files
- pom.xml
- src/main/java/com/example/whatsappai/WhatsAppAiAgentApplication.java
- src/main/java/com/example/whatsappai/controller/HealthController.java
- src/main/resources/application.yml
- src/test/java/com/example/whatsappai/WhatsAppAiAgentApplicationTests.java

### Deliverable
A runnable Spring Boot application that starts successfully.

---

## Phase 2 — WhatsApp Webhook Verification and Inbound Handling

### Goal
Accept inbound WhatsApp webhook requests and validate them before any downstream processing.

### What to build
- Webhook controller
- DTOs for incoming webhook payloads
- Validation logic for required fields
- Correlation ID generation
- Basic logging
- Simple inbound message normalization

### Key responsibilities
- Verify webhook requests
- Extract message content from webhook payload
- Validate required properties
- Create a normalized domain object for later processing

### Files
- src/main/java/com/example/whatsappai/controller/WebhookController.java
- src/main/java/com/example/whatsappai/dto/whatsapp/*.java
- src/main/java/com/example/whatsappai/domain/WhatsAppMessage.java
- src/main/java/com/example/whatsappai/service/InboundMessageService.java

### Deliverable
A working webhook endpoint that accepts WhatsApp events and converts them into a normalized application message.

---

## Phase 3 — Kafka-Based Asynchronous Processing

### Goal
Decouple webhook handling from AI processing using Kafka.

### What to build
- Kafka producer configuration
- Kafka consumer configuration
- Message event model
- Producer to publish inbound events
- Consumer to receive and process them asynchronously
- Basic error handling and logging

### Flow
Webhook Controller → InboundMessageService → Kafka Producer → Kafka Topic → Kafka Consumer

### Files
- src/main/java/com/example/whatsappai/kafka/WhatsAppMessageEvent.java
- src/main/java/com/example/whatsappai/kafka/producer/InboundMessageProducer.java
- src/main/java/com/example/whatsappai/kafka/consumer/InboundMessageConsumer.java
- src/main/java/com/example/whatsappai/config/KafkaConfig.java

### Deliverable
Inbound messages are processed asynchronously through Kafka.

---

## Phase 4 — Spring AI and OpenAI Integration

### Goal
Introduce AI orchestration using Spring AI and OpenAI.

### What to build
- AI orchestrator service
- Prompt service
- Spring AI ChatClient configuration
- OpenAI configuration properties
- Integration from Kafka consumer to AI layer

### Flow
Kafka Consumer → AIOrchestrator → PromptService → Spring AI → OpenAI

### Files
- src/main/java/com/example/whatsappai/ai/AIOrchestrator.java
- src/main/java/com/example/whatsappai/ai/PromptService.java
- src/main/java/com/example/whatsappai/config/AIConfig.java
- src/main/resources/application.yml

### Deliverable
A basic LLM-powered response is generated for inbound messages.

---

## Phase 5 — Redis-Based Conversation Memory

### Goal
Maintain short-term conversation context for each customer.

### What to build
- Redis connection configuration
- Conversation memory service
- Bounded conversation history storage
- TTL configuration
- AI orchestrator integration to load and save turns

### Files
- src/main/java/com/example/whatsappai/memory/ConversationMemoryService.java
- src/main/java/com/example/whatsappai/memory/ConversationTurn.java
- src/main/java/com/example/whatsappai/config/RedisConfig.java

### Deliverable
Recent conversation turns for each customer can be retrieved and used in prompts.

---

## Phase 6 — RAG with Embeddings and Vector Store

### Goal
Add retrieval-augmented generation so the assistant can answer using domain-specific knowledge.

### What to build
- Knowledge documents under resources
- Chunking and ingestion logic
- Embeddings generation
- Vector store integration
- Retrieval service
- Prompt augmentation with retrieved context

### Flow
User message → RAG retrieval → relevant context → prompt → LLM

### Files
- src/main/resources/knowledge/*.txt
- src/main/java/com/example/whatsappai/rag/KnowledgeIngestionService.java
- src/main/java/com/example/whatsappai/rag/RagService.java
- src/main/java/com/example/whatsappai/rag/DocumentChunk.java

### Deliverable
The LLM can answer questions using retrieved business knowledge rather than only general reasoning.

---

## Phase 7 — Spring AI Advisor-Based RAG Integration

### Goal
Modularize retrieval augmentation using Spring AI advisor concepts where supported by the chosen Spring AI version.

### What to build
- Advisor-based retrieval integration
- Refactor prompt construction if needed
- Keep orchestration logic clean and separated

### Files
- src/main/java/com/example/whatsappai/ai/AdvisorConfig.java

### Deliverable
Retrieval is integrated in a more modular and maintainable way.

---

## Phase 8 — Tool and Function Calling

### Goal
Allow the LLM to invoke controlled business tools for tasks such as checking order status.

### What to build
- Tool interface and implementation
- Mock order service
- Tool registration with Spring AI
- Input validation for tool arguments
- Logging around tool execution

### Files
- src/main/java/com/example/whatsappai/tools/OrderStatusTool.java
- src/main/java/com/example/whatsappai/service/OrderService.java
- src/main/java/com/example/whatsappai/domain/OrderStatus.java

### Deliverable
The model can call a tool for business-specific information instead of hallucinating it.

---

## Phase 9 — Structured LLM Output

### Goal
Make AI responses predictable for downstream application logic.

### What to build
- Structured output model
- Mapping from raw LLM response to structured Java object
- Validation of required fields
- Fallback behavior for malformed output

### Files
- src/main/java/com/example/whatsappai/ai/AgentResponse.java
- src/main/java/com/example/whatsappai/ai/StructuredResponseService.java

### Deliverable
Responses are validated and represented as a typed object.

---

## Phase 10 — WhatsApp Outbound Responses

### Goal
Send generated responses back to the customer through the WhatsApp API.

### What to build
- WhatsApp client interface
- HTTP-based implementation for outbound messaging
- Response service
- External configuration for credentials and endpoints
- Mock/local profile for testing without sending real messages

### Files
- src/main/java/com/example/whatsappai/client/WhatsAppClient.java
- src/main/java/com/example/whatsappai/client/WhatsAppCloudClient.java
- src/main/java/com/example/whatsappai/service/WhatsAppResponseService.java

### Deliverable
The application can return AI-generated replies to WhatsApp users.

---

## Phase 11 — Idempotency and Duplicate Protection

### Goal
Prevent double-processing of the same WhatsApp event.

### What to build
- Redis-backed idempotency service
- Message ID based deduplication
- Atomic claim logic using SET NX or similar
- Duplicate-event handling

### Files
- src/main/java/com/example/whatsappai/service/IdempotencyService.java

### Deliverable
Repeated webhook deliveries are safely ignored after the first successful processing.

---

## Phase 12 — Kafka Reliability, Retries, and DLT

### Goal
Make Kafka processing resilient to transient failures.

### What to build
- Retry handling for recoverable failures
- Backoff strategy
- Dead-letter topic support
- Clear separation between retryable and non-retryable errors

### Files
- src/main/java/com/example/whatsappai/config/KafkaErrorConfig.java

### Deliverable
Transient failures do not cause silent data loss, and poison messages can be isolated.

---

## Phase 13 — Rate Limiting

### Goal
Protect the application and LLM provider from excessive requests.

### What to build
- Redis-backed rate limiter
- Per-customer request limits
- Graceful response when the limit is exceeded

### Files
- src/main/java/com/example/whatsappai/service/RateLimitService.java

### Deliverable
The system can limit abuse and reduce unnecessary AI usage.

---

## Phase 14 — Guardrails and Prompt Injection Protection

### Goal
Reduce unsafe, manipulated, or hallucinated responses.

### What to build
- Input guardrail checks
- Output guardrail checks
- Prompt injection detection heuristics
- Safe fallback responses

### Files
- src/main/java/com/example/whatsappai/guardrail/InputGuardrail.java
- src/main/java/com/example/whatsappai/guardrail/OutputGuardrail.java
- src/main/java/com/example/whatsappai/guardrail/GuardrailService.java

### Deliverable
The AI layer is more robust against invalid input and suspicious prompts.

---

## Phase 15 — Token Optimization and Context Engineering

### Goal
Reduce token usage while preserving response quality.

### What to build
- Bounded conversation history
- Limited RAG retrieval size
- Cleaner system prompt
- Context assembly strategy
- Optional token usage tracking metadata

### Files
- src/main/java/com/example/whatsappai/ai/ContextBuilder.java

### Deliverable
Prompts remain efficient and cost-conscious.

---

## Phase 16 — Hallucination Reduction and Fallback Handling

### Goal
Make the assistant less likely to invent unsupported policies or facts.

### What to build
- Fallback behavior for missing context
- Preference for RAG and tools over general model guesses
- Graceful degradation for unavailable services

### Files
- src/main/java/com/example/whatsappai/service/FallbackService.java

### Deliverable
The system provides safe and grounded responses even when some components fail.

---

## Phase 17 — Correlation IDs and Observability

### Goal
Trace a request across the webhook, Kafka, AI, and outbound layers.

### What to build
- Correlation ID generation and propagation
- Logging with MDC
- Metrics and health information
- Basic observability support

### Files
- src/main/java/com/example/whatsappai/config/LoggingConfig.java
- src/main/java/com/example/whatsappai/util/CorrelationIdUtil.java

### Deliverable
A request can be traced end-to-end through the system.

---

## Phase 18 — Docker Compose and Local Infrastructure

### Goal
Run the application and dependent infrastructure locally with Docker Compose.

### What to build
- Kafka
- Redis
- PostgreSQL
- Optional PGVector support for RAG
- Environment variable-based configuration
- Example environment file

### Files
- docker-compose.yml
- .env.example

### Deliverable
The full prototype can be run locally with one command.

---

## Phase 19 — Integration Testing

### Goal
Verify the most important end-to-end flows.

### What to build
- Webhook to Kafka to AI processing tests
- Duplicate-message behavior tests
- RAG fallback tests
- Tool-calling tests
- Failure handling and retry tests

### Files
- src/test/java/com/example/whatsappai/integration/**

### Deliverable
The main user journeys are tested and documented.

---

## Phase 20 — Evaluation Framework

### Goal
Measure retrieval quality, response quality, and token efficiency instead of making unsupported claims.

### What to build
- Small evaluation dataset
- Evaluation runner
- Simple summary report

### Files
- src/test/java/com/example/whatsappai/evaluation/**

### Deliverable
The prototype can be evaluated and compared across iterations.

---

## Recommended Execution Order

1. Phase 1
2. Phase 2
3. Phase 3
4. Phase 4
5. Phase 5
6. Phase 6
7. Phase 8
8. Phase 9
9. Phase 10
10. Phase 11
11. Phase 12
12. Phase 13
13. Phase 14
14. Phase 15
15. Phase 16
16. Phase 17
17. Phase 18
18. Phase 19
19. Phase 20

> Phase 7 is optional and should be implemented only if the selected Spring AI version supports a clear advisor-based integration path.
