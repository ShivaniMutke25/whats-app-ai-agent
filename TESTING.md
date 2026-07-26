# Testing and verification

## Prerequisites

- Java 21 and Maven for local builds.
- Docker Desktop for containerized stack testing.
- The repository does not include a Maven wrapper, so install Maven or use Docker Compose for builds.

## Unit and module tests

Run all tests for the whole repository:

```bash
mvn test
```

Run service-specific tests:

```bash
mvn -pl gateway-service -am test
mvn -pl context-service -am test
mvn -pl ai-service -am test
mvn -pl outbound-service -am test
```

## Container smoke test

Start the full local stack:

```bash
docker compose up --build -d
```

Verify the key service endpoints:

```bash
curl "http://localhost:8081/whatsapp/webhook?hub.mode=subscribe&hub.challenge=ok&hub.verify_token=local-verify-token"
curl http://localhost:8082/inventory
```

Expected results:

- The gateway verification request returns the challenge string.
- The context service returns JSON for inventory.
- Gateway is available on `8081`, context service on `8082`, outbound on `8083`, and AI service on `8084`.

## Inbound webhook flow test

Post a valid WhatsApp webhook payload to the gateway:

```bash
curl -X POST http://localhost:8081/whatsapp/webhook \
  -H "Content-Type: application/json" \
  -d @whatsapp-webhook-sample.json
```

Then inspect the logs for AI and outbound activity:

```bash
docker compose logs ai-service outbound-service
```

Expected behavior:

- The gateway accepts and publishes the event to Kafka.
- The AI service processes the event asynchronously.
- The outbound service receives the reply request and logs delivery activity.

## Independent service checks

Run individual service stacks as needed:

```bash
docker compose up --build context-service
docker compose up --build outbound-service
docker compose up --build gateway-service kafka
docker compose up --build ai-service kafka redis context-service outbound-service
```

## Tear down

```bash
docker compose down
```

## Notes

- Local development uses mock outbound behavior unless `WHATSAPP_ACCESS_TOKEN` and `WHATSAPP_PHONE_NUMBER_ID` are configured.
- If `OPENAI_API_KEY` is not set, the AI service returns a safe fallback response.
- For production readiness, add durable idempotency and a persistent data store for context service state.
