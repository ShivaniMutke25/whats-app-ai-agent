# WhatsApp AI Agent

## Overview

This repository contains a Spring Boot prototype for a WhatsApp-based AI assistant targeting small shopkeepers.
The application receives WhatsApp webhook events, processes inbound messages asynchronously through Kafka, enriches them with business context, consults a lightweight RAG/AI layer, persists short-term conversation memory in Redis, and replies back through WhatsApp.

The current implementation includes:
- WhatsApp webhook handling and verification
- Kafka-based inbound event publishing and consumption
- Redis-backed conversation memory
- Spring AI OpenAI chat model integration
- Inventory and customer CRUD endpoints for shopkeeper context
- Basic guardrails for input/output safety
- In-memory idempotency and rate limiting
- Stubbed outbound WhatsApp response support for local development

## High-Level Architecture

```text
WhatsApp Cloud API
        |
        v
WebhookController (/whatsapp/webhook)
        |
InboundMessageService
        |
IdempotencyService (webhook-level)
        |
InboundMessageProducer -> Kafka Topic (whatsapp.inbound)
        |
Kafka Consumer (InboundMessageConsumer)
        |
IdempotencyService (Kafka consumer-level)
        |
RateLimitService
        |
AIOrchestrator
   ├── ConversationMemoryService (Redis)
   ├── RagService (stub retrieval)
   ├── BusinessContextService (inventory + customer data)
   ├── SpringAiChatService (Spring AI / OpenAI)
   ├── GuardrailService / InputGuardrail / OutputGuardrail
   └── StructuredResponseService
        |
WhatsAppResponseService -> WhatsAppClient
        |
WhatsApp Cloud API or mock reply
```

## Package Structure

- `controller/` - REST endpoints for WhatsApp webhook, inventory, customers, shopkeeper overview, and health.
- `service/` - business services, including inbound normalization, inventory/customer CRUD, idempotency, rate limiting, fallback, and outbound WhatsApp integration.
- `kafka/` - message event model plus producer and consumer logic.
- `memory/` - Redis-backed conversation memory storage and turn history.
- `ai/` - AI orchestration, prompt generation, business context assembly, Spring AI chat invocation, and structured response normalization.
- `rag/` - retrieval augmentation service stubs and document model.
- `guardrail/` - input/output safety checks and simple prompt-injection prevention.
- `config/` - Redis and Kafka configuration beans.
- `domain/` - domain records for inventory items, customer profiles, and WhatsApp messages.

## Low-Level Design

### WhatsApp Webhook Flow

1. `WebhookController` receives verification or inbound POST webhook.
2. `InboundMessageService` normalizes the WhatsApp payload to `WhatsAppMessage`.
3. `IdempotencyService` prevents duplicate webhook message processing.
4. `InboundMessageProducer` publishes a `WhatsAppMessageEvent` to Kafka.
5. `InboundMessageConsumer` listens on `whatsapp.inbound` and reconstructs `WhatsAppMessage`.
6. The consumer re-checks idempotency and rate limits.
7. The message is forwarded to `AIOrchestrator`.

### AI Orchestration

`AIOrchestrator` performs:
- input guardrail validation
- conversation history loading from Redis
- business context enrichment using inventory/customer data
- RAG document retrieval from `RagService`
- prompt construction via `PromptService`
- AI invocation through `SpringAiChatService`
- structured response normalization and output sanitization
- saving assistant replies to conversation memory

### Business Context

`BusinessContextService` enriches prompts with:
- customer profile details (when available)
- inventory summary if the incoming message mentions inventory

### Memory

`ConversationMemoryService` stores the last turns in Redis under keys like `whatsapp:conversation:{customerId}`.
It keeps up to 6 turns and sets a TTL of 1 hour.

### RAG

`RagService` provides lightweight domain hints through a static document set.
This is currently a stub and can be extended to use real embeddings/vector search.

### Spring AI Integration

`SpringAiConfig` exposes a `ChatModel` bean using `OpenAiChatModel`.
`SpringAiChatService` builds a `Prompt` with `SystemMessage` and `UserMessage`, then calls the model.

### WhatsApp Outbound

`WhatsAppResponseService` delegates outbound messaging to `WhatsAppClient`.
If WhatsApp credentials are absent, it falls back to `MockWhatsAppClient` for local testing.

## Running Locally

### Prerequisites

- Java 21
- Maven
- Docker and Docker Compose (for Kafka + Redis)

### Start Kafka and Redis

```bash
cd /workspaces/whats-app-ai-agent
docker-compose up -d
```

### Build and run the application

```bash
mvn clean package
mvn spring-boot:run
```

Alternatively:

```bash
java -jar target/whatsapp-ai-agent-0.0.1-SNAPSHOT.jar
```

### Environment Variables

The application reads configuration from both `application.yml` and environment variables.
Key settings:

- `KAFKA_BOOTSTRAP_SERVERS` (default: `localhost:9092`)
- `REDIS_HOST` (default: `localhost`)
- `REDIS_PORT` (default: `6379`)
- `OPENAI_API_KEY` (required for real AI calls)
- `OPENAI_MODEL` (default: `gpt-4o-mini`)
- `OPENAI_BASE_URL` (default: `https://api.openai.com/v1`)
- `WHATSAPP_ACCESS_TOKEN` (required for real WhatsApp outbound)
- `WHATSAPP_PHONE_NUMBER_ID` (required for real WhatsApp outbound)
- `WHATSAPP_API_BASE_URL` (default: `https://graph.facebook.com/v20.0`)
- `SPRING_AI_OPENAI_CHAT_API_KEY` (optional override for Spring AI)
- `SPRING_AI_OPENAI_CHAT_MODEL` (optional override)

### Local Testing

- Use the webhook verification endpoint:
  `GET http://localhost:8080/whatsapp/webhook?hub.mode=subscribe&hub.challenge=challenge&hub.verify_token=local-verify-token`
- Post a WhatsApp-style payload to `POST http://localhost:8080/whatsapp/webhook`.
- Inspect inventory with `GET http://localhost:8080/inventory`.
- Inspect customers with `GET http://localhost:8080/customers`.

## Example Webhook Request

The application expects a WhatsApp webhook JSON payload with nested `entry`, `changes`, and `messages` fields.
A sample payload is available in the existing DTO structure under `dto.whatsapp`.

## What Works Today

- Basic webhook ingestion and verification
- Kafka publish/consume path
- Redis-based recent conversation history
- Spring AI prompt generation and OpenAI chat invocation
- Inventory and customer REST CRUD endpoints
- Guardrails for input/output safety
- Local mock WhatsApp outbound path

## Current Limitations

- `RagService` is a stub and not a full embeddings search system.
- `OrderStatusTool` is currently a hard-coded stub.
- WhatsApp outbound is mocked if credentials are missing.
- Idempotency is in-memory and resets on restart.
- No full tool/function-calling schema is implemented yet.
- No persistent database is configured for inventory/customer data.

## Next Steps

Potential enhancements:
- replace stubbed RAG with a real embeddings/vector store
- add persistent storage for inventory/customers
- implement WhatsApp Cloud message sending and templates
- improve idempotency with Redis or durable store
- add structured tool definitions and response schemas
- implement DLT/retry patterns for Kafka processing

## Useful Source Locations

- `src/main/java/com/example/whatsappai/controller/WebhookController.java`
- `src/main/java/com/example/whatsappai/kafka/producer/InboundMessageProducer.java`
- `src/main/java/com/example/whatsappai/kafka/consumer/InboundMessageConsumer.java`
- `src/main/java/com/example/whatsappai/ai/AIOrchestrator.java`
- `src/main/java/com/example/whatsappai/ai/SpringAiChatService.java`
- `src/main/java/com/example/whatsappai/memory/ConversationMemoryService.java`
- `src/main/java/com/example/whatsappai/service/WhatsAppResponseService.java`
- `src/main/java/com/example/whatsappai/config/RedisConfig.java`
- `src/main/java/com/example/whatsappai/config/KafkaConfig.java`

---

This documentation reflects the current codebase state and should help onboard new contributors and resume development from the existing implementation.
