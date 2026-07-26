# WhatsApp AI Agent Architecture

## Purpose

This repository implements a small WhatsApp AI assistant as an event-driven microservice system. The design separates inbound webhook ingestion, business context, AI orchestration, and outbound delivery into independently deployable Spring Boot services.

## High-level design (LD)

The system is organized into four service boundaries plus a shared library:

- `gateway-service` (port `8081`): receives WhatsApp webhook callbacks, validates and normalizes incoming events, deduplicates them, and publishes them to Kafka.
- `context-service` (port `8082`): hosts customer, inventory, and shopkeeper data APIs used by the AI orchestrator.
- `ai-service` (port `8084`): consumes normalized inbound events from Kafka, enriches them with context and conversation memory, manages guardrails and prompt orchestration, and forwards replies to outbound delivery.
- `outbound-service` (port `8083`): accepts internal outbound requests and sends WhatsApp messages or uses a mock delivery mode when credentials are missing.
- `common`: shared transport and domain model library used by all services; not deployed as a service.

## Functionalities

- Inbound webhook normalization and handshake verification for WhatsApp Cloud API.
- Asynchronous event routing through Kafka topic `whatsapp.inbound`.
- Idempotent webhook ingestion and message deduplication.
- Conversation memory stored in Redis for reuse across messages.
- Context lookups for customers, inventory, and shopkeeper data.
- AI prompt construction, guardrail enforcement, and optional OpenAI fallback behavior.
- Outbound delivery via WhatsApp Cloud API or local mock mode.
- Independent service deployment and versioned endpoints.

## Data flow

```text
WhatsApp Cloud API
      | webhook
      v
Gateway (8081)
      | publish
      v
Kafka topic `whatsapp.inbound`
      | consume
      v
AI service (8084)
      | HTTP -> Redis
      | HTTP -> Context service (8082)
      | HTTP -> Outbound service (8083)
      v
Outbound service (8083)
      | send
      v
WhatsApp Cloud API / mock
```

### Request lifecycle

1. A WhatsApp webhook reaches `gateway-service` at `/whatsapp/webhook`.
2. Gateway validates the webhook, extracts the normalized message payload, and publishes it to Kafka.
3. `ai-service` consumes the event from topic `whatsapp.inbound` as part of `whatsapp-ai-group`.
4. AI performs idempotency checks, loads conversation history from Redis, and fetches business context from `context-service`.
5. It constructs a prompt, invokes the AI model or fallback logic, then sends a delivery request to `outbound-service`.
6. `outbound-service` posts the response to WhatsApp Cloud API if credentials are present, otherwise logs a mock delivery.

## Service boundaries and APIs

| Service | Responsibilities | Public/Inbound API | Dependencies |
| --- | --- | --- | --- |
| `gateway-service` | Webhook validation, event normalization, request deduplication, Kafka publishing | `GET/POST /whatsapp/webhook` | Kafka |
| `context-service` | Customer/inventory data, shopkeeper views, business context | `GET /customers`, `GET /inventory`, `GET /shopkeeper/*` | none |
| `ai-service` | Kafka consumption, prompt orchestration, AI guardrails, Redis memory, context enrichment, outbound requests | internal Kafka consumer | Redis, `context-service`, `outbound-service` |
| `outbound-service` | Message formatting, WhatsApp Cloud integration, mock delivery fallback | `POST /internal/messages` | WhatsApp Cloud API or mock client |

## Low-level design (LLD)

- `gateway-service`
  - Runs on port `8081`.
  - Accepts webhook verification and inbound messages.
  - Uses in-memory deduplication for local development.
  - Publishes normalized events to Kafka topic `whatsapp.inbound`.

- `context-service`
  - Runs on port `8082`.
  - Serves in-memory business data for customers and inventory.
  - Provides shopkeeper endpoints for operational views.

- `ai-service`
  - Runs on port `8084`.
  - Consumes messages from Kafka using consumer group `whatsapp-ai-group`.
  - Reads and updates conversation history in Redis.
  - Fetches customer and inventory information from `context-service`.
  - Calls `outbound-service` for reply delivery.
  - Uses `OPENAI_API_KEY` when available; otherwise returns a safe fallback response.

- `outbound-service`
  - Runs on port `8083`.
  - Receives internal outbound payloads at `/internal/messages`.
  - Sends WhatsApp messages using configured credentials.
  - Falls back to mock sending when tokens or phone IDs are absent.

- `common`
  - Contains shared event payloads, domain models, and Kafka record definitions.
  - Ensures compatibility across services without introducing deploy-time dependencies.

## Deployment and configuration

- Local composition uses `docker-compose.yml` and exposes services on host ports `8081`, `8082`, `8083`, and `8084`.
- Kafka is the asynchronous boundary; Redis stores conversation memory.
- Internal service URLs are configured via environment variables, not Java module dependencies.

### Important environment variables

- `KAFKA_BOOTSTRAP_SERVERS` — Kafka broker address
- `REDIS_HOST` — Redis host address
- `CONTEXT_SERVICE_URL` — `http://context-service:8082`
- `OUTBOUND_SERVICE_URL` — `http://outbound-service:8083`
- `OPENAI_API_KEY` — OpenAI API key; optional for safe fallback
- `WHATSAPP_ACCESS_TOKEN` — WhatsApp API bearer token
- `WHATSAPP_PHONE_NUMBER_ID` — WhatsApp phone number ID

## Production considerations

- Replace in-memory deduplication with durable Redis or database-backed idempotency.
- Move `context-service` data from in-memory storage to a service-owned persistent database.
- Add observability, tracing, and centralized logging for multi-service flow.
- Scale `ai-service` consumers and outbound workers independently as load increases.
- Ensure WhatsApp credentials and OpenAI keys are securely managed and rotated.
