# WhatsApp AI Agent microservices

This project implements a WhatsApp AI assistant using independently deployable Spring Boot services.

- `gateway-service` (port `8081`): validates WhatsApp webhooks and publishes normalized `whatsapp.inbound` events to Kafka.
- `context-service` (port `8082`): provides customer, inventory, and shopkeeper APIs.
- `ai-service` (port `8084`): consumes inbound events, uses Redis memory and context HTTP APIs, and requests delivery.
- `outbound-service` (port `8083`): sends WhatsApp replies or uses a local mock client.
- `common`: shared domain and transport model library; it is not deployed.

## Run the full stack locally

1. Copy `.env.example` to `.env`.
2. Set credentials only when real AI or WhatsApp integration is needed.
3. Start the stack:

```bash
docker compose up --build
```

Service endpoints:

- Gateway webhook: `http://localhost:8081/whatsapp/webhook`
- Context APIs: `http://localhost:8082`
- Outbound service: `http://localhost:8083`
- AI service: `http://localhost:8084`

## Build a single service

```bash
mvn -pl gateway-service -am package
java -jar gateway-service/target/gateway-service-*.jar
```

Replace `gateway-service` with `context-service`, `ai-service`, or `outbound-service` as required.

## Key configuration

| Variable | Used by | Default |
| --- | --- | --- |
| `KAFKA_BOOTSTRAP_SERVERS` | gateway, AI | `kafka:9092` |
| `REDIS_HOST` | AI | `redis` |
| `CONTEXT_SERVICE_URL` | AI | `http://context-service:8082` |
| `OUTBOUND_SERVICE_URL` | AI | `http://outbound-service:8083` |
| `OPENAI_API_KEY` | AI | empty (safe fallback) |
| `WHATSAPP_ACCESS_TOKEN` | outbound | empty (mock) |
| `WHATSAPP_PHONE_NUMBER_ID` | outbound | empty (mock) |

For architecture details, usage examples, and verification steps, see [ARCHITECTURE.md](ARCHITECTURE.md), [USAGE.md](USAGE.md), and [TESTING.md](TESTING.md).
