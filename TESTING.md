# Testing and verification

## Prerequisites

- Java 21 and Maven for local builds, or Docker Desktop for container builds.
- The current workspace does not include a Maven wrapper; install Maven or use Docker Compose.

## Unit/build tests

```bash
mvn test
mvn -pl gateway-service -am test
mvn -pl context-service -am test
mvn -pl ai-service -am test
mvn -pl outbound-service -am test
```

## Container smoke test

```bash
docker compose up --build -d
docker compose ps
curl "http://localhost:8081/whatsapp/webhook?hub.mode=subscribe&hub.challenge=ok&hub.verify_token=local-verify-token"
curl http://localhost:8082/inventory
```

The first request must return `ok`; the inventory request must return JSON (possibly an empty list).

## Webhook flow

Post a valid WhatsApp webhook payload to `http://localhost:8081/whatsapp/webhook`. Expect HTTP `202` and inspect `docker compose logs ai-service outbound-service` for the asynchronous handling and mock-delivery entry.

## Independent service checks

```bash
docker compose up --build context-service
docker compose up --build outbound-service
docker compose up --build gateway-service kafka
docker compose up --build ai-service kafka redis context-service outbound-service
```

Stop the stack with `docker compose down`.
