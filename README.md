# WhatsApp AI Agent — microservices

The monolith has been split into independently deployable Spring Boot services:

- `gateway-service` (port 8081): validates WhatsApp webhooks and publishes `whatsapp.inbound` events.
- `context-service` (port 8082): inventory and customer CRUD APIs.
- `ai-service` (port 8084): consumes inbound events, uses Redis memory and context HTTP APIs, then requests delivery.
- `outbound-service` (port 8083): sends WhatsApp replies or uses the local mock client.
- `common`: shared event and API model library; it is not deployed.

## Run everything

1. Copy `.env.example` to `.env` and set credentials only when real AI/WhatsApp integration is needed.
2. Start the stack:

```bash
docker compose up --build
```

Gateway is available at `http://localhost:8081`. Context APIs are at `http://localhost:8082`.

The stack works without `OPENAI_API_KEY`: AI service returns a safe configuration message and outbound is mocked by default.

## Build a single service

```bash
mvn -pl gateway-service -am package
java -jar gateway-service/target/gateway-service-*.jar
```

Use the matching module name for `context-service`, `ai-service`, or `outbound-service`.

## Key configuration

| Variable | Used by | Default |
| --- | --- | --- |
| `KAFKA_BOOTSTRAP_SERVERS` | gateway, AI | `kafka:9092` |
| `REDIS_HOST` | AI | `redis` |
| `CONTEXT_SERVICE_URL` | AI | `http://context-service:8082` |
| `OUTBOUND_SERVICE_URL` | AI | `http://outbound-service:8083` |
| `OPENAI_API_KEY` | AI | empty (safe fallback) |
| `WHATSAPP_ACCESS_TOKEN`, `WHATSAPP_PHONE_NUMBER_ID` | outbound | empty (mock) |

See [ARCHITECTURE.md](ARCHITECTURE.md) and [TESTING.md](TESTING.md) for interfaces and verification.
