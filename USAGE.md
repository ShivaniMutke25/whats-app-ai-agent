# WhatsApp AI Agent Usage Guide

## Quick Start

### Start locally with Docker Compose

```bash
docker compose up --build -d
```

The service ports exposed on localhost are:

- Gateway webhook: `http://localhost:8081`
- Context service: `http://localhost:8082`
- Outbound service: `http://localhost:8083`
- AI service: `http://localhost:8084`

### Run a single service locally with Maven

```bash
mvn -pl gateway-service -am package
java -jar gateway-service/target/gateway-service-*.jar
```

Replace `gateway-service` with `context-service`, `ai-service`, or `outbound-service` as needed.

## Verify Webhook Endpoint

Use this command against `gateway-service`:

```bash
curl "http://localhost:8081/whatsapp/webhook?hub.mode=subscribe&hub.challenge=CHALLENGE_TOKEN&hub.verify_token=local-verify-token"
```

A successful response returns the challenge token.

## Sample Inbound Webhook Payload

Save the following JSON to `whatsapp-webhook-sample.json`:

```json
{
  "object": "whatsapp_business_account",
  "entry": [
    {
      "changes": [
        {
          "value": {
            "metadata": {
              "display_phone_number": "123456789",
              "phone_number_id": "987654321"
            },
            "messages": [
              {
                "from": "447700900123",
                "id": "wamid.HBgMNTg3MjM0NTY3ODkwFQIAEhgUMDI3OTQ5MDkyOQAFGgQ0AA==",
                "timestamp": 1700000000,
                "type": "text",
                "text": {
                  "body": "Hello, do you have more stock for item ITEM-002?"
                }
              }
            ]
          }
        }
      ]
    }
  ]
}
```

Send it to the gateway:

```bash
curl -X POST http://localhost:8081/whatsapp/webhook \
  -H "Content-Type: application/json" \
  -d @whatsapp-webhook-sample.json
```

The gateway normalizes the inbound message, publishes it to Kafka, and returns an accepted response.

## Inspect Context Service Data

Check inventory:

```bash
curl http://localhost:8082/inventory
```

Check customers:

```bash
curl http://localhost:8082/customers
```

Check shopkeeper overview endpoints:

```bash
curl http://localhost:8082/shopkeeper/inventory
curl http://localhost:8082/shopkeeper/customers
```

## Production Testing

### Required production environment variables

- `KAFKA_BOOTSTRAP_SERVERS` — Kafka brokers
- `REDIS_HOST` — Redis host
- `REDIS_PORT` — Redis port
- `OPENAI_API_KEY` — OpenAI API key
- `OPENAI_MODEL` — Model to use (for example `gpt-4o-mini`)
- `WHATSAPP_ACCESS_TOKEN` — WhatsApp Cloud API bearer token
- `WHATSAPP_PHONE_NUMBER_ID` — WhatsApp Phone Number ID
- `WHATSAPP_API_BASE_URL` — WhatsApp base URL (default: `https://graph.facebook.com/v20.0`)

### Start in production mode

Use a process manager or container runtime to launch the required services. Example:

```bash
OPENAI_API_KEY=your_key \
WHATSAPP_ACCESS_TOKEN=your_whatsapp_token \
WHATSAPP_PHONE_NUMBER_ID=your_phone_number_id \
KAFKA_BOOTSTRAP_SERVERS=kafka-host:9092 \
REDIS_HOST=redis-host \
REDIS_PORT=6379 \
java -jar ai-service/target/ai-service-*.jar
```

### Production smoke test

1. Confirm the gateway is reachable:

```bash
curl "http://localhost:8081/whatsapp/webhook?hub.mode=subscribe&hub.challenge=CHALLENGE_TOKEN&hub.verify_token=local-verify-token"
```

2. Confirm the context service is online:

```bash
curl http://localhost:8082/inventory
```

3. Send a valid WhatsApp webhook payload to `http://localhost:8081/whatsapp/webhook`.
4. Monitor logs for Kafka publish/consume activity and AI orchestration.
5. Verify outbound replies are sent by WhatsApp Cloud API or observed in mock mode.

## Production Notes

- Kafka is used for asynchronous inbound message delivery.
- Redis stores recent conversation history for the AI service.
- If `WHATSAPP_ACCESS_TOKEN` or `WHATSAPP_PHONE_NUMBER_ID` is missing, outbound delivery uses mock mode.
- For real WhatsApp delivery, provide both credentials.
- Durable idempotency and a persistent database for context data are recommended for production.

## Debugging

- Use `docker compose logs kafka` and `docker compose logs redis` when running locally.
- Check connectivity for `KAFKA_BOOTSTRAP_SERVERS`, `REDIS_HOST`, and `REDIS_PORT`.
- Validate `OPENAI_API_KEY` and `WHATSAPP_ACCESS_TOKEN` when using real external services.

## Real WhatsApp Cloud Integration

With valid credentials, the app forwards outbound messages through the WhatsApp Cloud API.

Important:
- Set a valid access token and phone number ID.
- Ensure the incoming webhook `phone_number_id` matches the configured `WHATSAPP_PHONE_NUMBER_ID`.
- Confirm that webhook requests are routed to `gateway-service` on port `8081`.

## What to Expect

- The gateway returns `accepted` when inbound normalization and Kafka publishing succeed.
- The AI service consumes the event asynchronously.
- AI orchestration constructs a prompt, calls the model or fallback path, and stores history in Redis.
- Outbound replies are delivered through WhatsApp if configured, otherwise mocked for local testing.
