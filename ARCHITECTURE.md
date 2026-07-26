# WhatsApp AI Agent Architecture

## Purpose

This document provides a dedicated architecture reference for the current `whats-app-ai-agent` codebase.
It explains system flow, component design, integration points, and how to run/use the application.

## System Overview

The application is a Spring Boot backend that processes WhatsApp webhook messages, enriches them with business context, sends them to an AI model, and returns responses.
It is designed to support small shopkeepers with inventory and customer-aware assistance.

## High-Level Flow

1. WhatsApp Cloud API delivers webhook events to `WebhookController`.
2. `InboundMessageService` normalizes webhook payloads into `WhatsAppMessage`.
3. `IdempotencyService` prevents duplicate processing at webhook and Kafka consumer stages.
4. `InboundMessageProducer` publishes normalized events to Kafka topic `whatsapp.inbound`.
5. `InboundMessageConsumer` consumes events asynchronously.
6. `AIOrchestrator` processes the message using memory, RAG, guardrails, and Spring AI.
7. `WhatsAppResponseService` sends the AI-generated reply back through WhatsApp or mock mode.

## Component Architecture

### Controllers

- `WebhookController`
  - Receives WhatsApp webhook verification and inbound POSTs.
  - Normalizes payload, deduplicates, and publishes events.
- `InventoryController`
  - CRUD API for inventory items.
- `CustomerController`
  - CRUD API for shopkeeper customer profiles.
- `ShopkeeperController`
  - Convenience endpoints for inventory and customer overview.

### Services

- `InboundMessageService`
  - Converts incoming WhatsApp JSON into core domain object `WhatsAppMessage`.
- `IdempotencyService`
  - In-memory duplicate prevention for message IDs.
- `RateLimitService`
  - Basic per-customer rate limiting.
- `WhatsAppResponseService`
  - Sends outbound replies via `WhatsAppClient`.
- `FallbackService`
  - Provides safe fallback text when AI cannot respond.
- `InventoryService` / `CustomerService`
  - In-memory domain CRUD for shopkeeper data.

### AI Layer

- `AIOrchestrator`
  - Central coordination point for AI request processing.
  - Calls memory, RAG, business context, prompt builder, AI service, and response normalization.
- `ConversationMemoryService`
  - Stores recent conversation turns in Redis.
  - Keeps up to 6 turns and expires history after 1 hour.
- `BusinessContextService`
  - Adds inventory and customer profile details to prompts.
- `RagService`
  - Provides static contextual documents for relevant queries.
- `PromptService`
  - Builds prompt text combining system instructions, business context, history, retrieved docs, and customer message.
- `SpringAiChatService`
  - Calls Spring AI `ChatModel` through `OpenAiChatModel`.
  - Wraps prompts in `SystemMessage` and `UserMessage`.
- `StructuredResponseService`
  - Normalizes raw AI output into `AgentResponse`.
- `GuardrailService`, `InputGuardrail`, `OutputGuardrail`
  - Ensure safe input and output content.

### Messaging and Integration

- `KafkaConfig`
  - Configures a Kafka producer template for `WhatsAppMessageEvent`.
- `InboundMessageProducer`
  - Sends normalized inbound events to Kafka.
- `InboundMessageConsumer`
  - Reads events from the Kafka topic and invokes AI orchestration.

### Redis

- `RedisConfig`
  - Creates a Lettuce connection factory and `StringRedisTemplate`.
- `ConversationMemoryService`
  - Uses Redis string storage to persist JSON-serialized conversation turns.

## Data Models

- `WhatsAppMessage`
  - Core inbound message record, including `customerId`, `phoneNumber`, `message`, timestamp, and correlation ID.
- `WhatsAppMessageEvent`
  - Kafka event record used between webhook ingestion and consumer processing.
- `InventoryItem`
  - Shopkeeper inventory entity with SKU, quantity, and price.
- `CustomerProfile`
  - Shopkeeper customer profile used for context enrichment.
- `ConversationTurn`
  - Redis conversation history entry with role and content.
- `AgentResponse`
  - AI response model containing answer, category, confidence, and support flag.
- `RetrievedDocument`
  - RAG context item with content and relevance score.

## Runtime Configuration

### Main configuration file

- `src/main/resources/application.yml`

### Important settings

- `spring.kafka.bootstrap-servers` - Kafka broker address
- `spring.redis.host`, `spring.redis.port` - Redis connection
- `server.port` - Application port
- `app.kafka.topic.inbound` - Kafka inbound topic
- `app.kafka.consumer.group-id` - Kafka consumer group ID
- `app.whatsapp.api.base-url` - WhatsApp API base URL
- `app.whatsapp.api.access-token` - WhatsApp access token
- `app.whatsapp.phone-number-id` - WhatsApp phone number ID
- `spring.ai.openai.chat.api-key` - OpenAI API key for Spring AI
- `spring.ai.openai.chat.options.model` - OpenAI chat model name

## How to Run

### Start dependencies

```bash
cd /workspaces/whats-app-ai-agent
docker-compose up -d
```

### Run the application

```bash
mvn clean package
mvn spring-boot:run
```

### Local development notes

- The app defaults to mock WhatsApp outbound mode when no `WHATSAPP_ACCESS_TOKEN` or `WHATSAPP_PHONE_NUMBER_ID` is provided.
- Redis and Kafka are expected at `localhost:6379` and `localhost:9092` by default.

## How to Use

### Verify webhook

```http
GET /whatsapp/webhook?hub.mode=subscribe&hub.challenge=challenge&hub.verify_token=local-verify-token
```

### Send inbound WhatsApp payload

POST to `/whatsapp/webhook` with a WhatsApp webhook JSON body.

### Inspect shopkeeper data

- `GET /inventory`
- `GET /customers`
- `GET /shopkeeper/inventory`
- `GET /shopkeeper/customers`

### Example flow

1. Webhook call arrives.
2. Message is normalized and published to Kafka.
3. Kafka consumer runs AI orchestration.
4. Redis memory enriches the prompt.
5. Spring AI returns a reply.
6. Reply is sent to WhatsApp or mocked locally.

## Current Capabilities

- Webhook ingestion and verification
- Kafka-based asynchronous processing
- Redis-backed session memory
- Spring AI/OpenAI chat invocation
- Inventory/customer REST APIs
- Guardrails and safe fallback behavior
- Mocked outbound WhatsApp support for testing

## Known Gaps

- RAG is a static stub, not a real embeddings-based retrieval system.
- Tool/function calling is only stubbed via `OrderStatusTool`.
- Inventory/customer data is in-memory only.
- Idempotency is not durable across restarts.
- WhatsApp outbound is mocked without real credentials.

## Recommended Next Steps

- Add persistent data stores for inventory and customers.
- Replace `RagService` with an embeddings vector search.
- Persist idempotency state in Redis or database.
- Implement real WhatsApp Cloud API outbound messaging.
- Add error handling and dead-letter topic support for Kafka.
- Add structured tool definitions and richer response parsing.
