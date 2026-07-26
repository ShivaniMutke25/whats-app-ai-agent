# Architecture

```text
WhatsApp Cloud API
   | webhook
   v
Gateway :8081 -- Kafka topic `whatsapp.inbound` --> AI :8084
                                                    |  \-- Redis (conversation memory)
                                                    | HTTP
                                                    +--> Context :8082 (customers, inventory)
                                                    | HTTP
                                                    +--> Outbound :8083 --> WhatsApp Cloud API / mock
```

## Boundaries

| Service | Owns | Inbound interface | Outbound dependency |
| --- | --- | --- | --- |
| Gateway | webhook normalization and ingress deduplication | `GET/POST /whatsapp/webhook` | Kafka |
| Context | customer and inventory data | `/customers`, `/inventory`, `/shopkeeper/*` | none |
| AI | guardrails, prompting, rate limits, memory, orchestration | Kafka consumer group `whatsapp-ai-group` | Redis, Context HTTP, Outbound HTTP |
| Outbound | WhatsApp delivery | `POST /internal/messages` | WhatsApp Cloud API |

Kafka is the asynchronous boundary. Context and outbound calls are synchronous internal HTTP calls, configured through URLs rather than Java module dependencies. Each deployable module has its own Spring application class, `application.yml`, Maven executable JAR, Dockerfile, and port.

`common` contains only shared transport/domain models. It has no deployable application.

## Operational notes

- Gateway and AI use independent in-memory deduplication today; restart clears it. Replace with Redis/database idempotency for production.
- Context data is in-memory and should be moved to a service-owned database before production.
- AI runs safely without an OpenAI key, returning a configuration fallback.
- Outbound defaults to mock behavior when WhatsApp credentials are absent.
