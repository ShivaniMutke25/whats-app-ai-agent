# WhatsApp AI Agent Usage Guide

## Quick Start

### Start dependencies locally

```bash
cd /workspaces/whats-app-ai-agent
docker-compose up -d
```

### Run the application

```bash
mvn clean package
mvn spring-boot:run
```

The service will start at `http://localhost:8080` by default.

## Verify Webhook Endpoint

Use this command to verify the webhook URL with WhatsApp Cloud-style verification parameters:

```bash
curl "http://localhost:8080/whatsapp/webhook?hub.mode=subscribe&hub.challenge=CHALLENGE_TOKEN&hub.verify_token=local-verify-token"
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

Send it to the running app:

```bash
curl -X POST http://localhost:8080/whatsapp/webhook \
  -H "Content-Type: application/json" \
  -d @whatsapp-webhook-sample.json
```

The endpoint will normalize the inbound message, publish it to Kafka, and return an accepted response.

## Inspect Shopkeeper Data

Check inventory:

```bash
curl http://localhost:8080/inventory
```

Check customers:

```bash
curl http://localhost:8080/customers
```

Check shopkeeper overview endpoints:

```bash
curl http://localhost:8080/shopkeeper/inventory
curl http://localhost:8080/shopkeeper/customers
```

## Production Testing

### Required production environment variables

- `KAFKA_BOOTSTRAP_SERVERS` - Kafka brokers
- `REDIS_HOST` - Redis host
- `REDIS_PORT` - Redis port
- `OPENAI_API_KEY` - OpenAI API key
- `OPENAI_MODEL` - Model to use (e.g. `gpt-4o-mini`)
- `WHATSAPP_ACCESS_TOKEN` - WhatsApp Cloud API bearer token
- `WHATSAPP_PHONE_NUMBER_ID` - WhatsApp Phone Number ID
- `WHATSAPP_API_BASE_URL` - WhatsApp base URL (default: `https://graph.facebook.com/v20.0`)

### Start in production mode

Use a production-ready process manager or container runtime to start the app.
For example:

```bash
OPENAI_API_KEY=your_key \
WHATSAPP_ACCESS_TOKEN=your_whatsapp_token \
WHATSAPP_PHONE_NUMBER_ID=your_phone_number_id \
KAFKA_BOOTSTRAP_SERVERS=kafka-host:9092 \
REDIS_HOST=redis-host \
REDIS_PORT=6379 \
java -jar target/whatsapp-ai-agent-0.0.1-SNAPSHOT.jar
```

### Production smoke test

1. Confirm the app is healthy:

```bash
curl http://localhost:8080/actuator/health
```

2. Confirm the webhook endpoint is reachable:

```bash
curl "http://localhost:8080/whatsapp/webhook?hub.mode=subscribe&hub.challenge=CHALLENGE_TOKEN&hub.verify_token=local-verify-token"
```

3. Send a real or test WhatsApp webhook payload to `/whatsapp/webhook`.
4. Monitor logs for Kafka publish/consume activity and Spring AI invocation.
5. Verify outbound replies are sent by the WhatsApp Cloud API.

## Production Notes

- The app uses Kafka for asynchronous processing.
- Redis stores the recent conversation history for each customer.
- If `WHATSAPP_ACCESS_TOKEN` or `WHATSAPP_PHONE_NUMBER_ID` is missing, outgoing replies fall back to a mock client.
- For real WhatsApp message delivery, both credentials must be provided.
- The current idempotency service is in-memory; for production, a durable Redis or database-backed idempotency layer is recommended.

## Debugging

- Check `docker-compose logs kafka` and `docker-compose logs redis` when running locally.
- Confirm Kafka broker connectivity with `KAFKA_BOOTSTRAP_SERVERS`.
- Confirm Redis connectivity with `REDIS_HOST` and `REDIS_PORT`.
- Validate `OPENAI_API_KEY` and `WHATSAPP_ACCESS_TOKEN` are set when using real external services.

## Real WhatsApp Cloud Integration

Once the app is receiving real webhook events and has valid credentials, it will attempt to send outbound replies using `WhatsAppCloudClient`.

Important:
- Provide a valid access token and phone number ID.
- Use the WhatsApp Cloud API base URL if required by your environment.
- Confirm the `phone_number_id` field in incoming webhooks matches the `WHATSAPP_PHONE_NUMBER_ID` used for outbound sending.

## Sample Production `curl` Payload

Use the same sample payload as above, but ensure the `from` phone number is formatted correctly for your WhatsApp test user and the `id` value is unique.

```bash
curl -X POST https://your.production.host/whatsapp/webhook \
  -H "Content-Type: application/json" \
  -d @whatsapp-webhook-sample.json
```

## What to Expect

- The webhook call returns `accepted` if message normalization and publishing succeed.
- The Kafka consumer processes the event asynchronously.
- AI orchestration builds a prompt, calls OpenAI, and stores history in Redis.
- The reply is sent through WhatsApp if outbound credentials are configured.
- If credentials are missing, the message is not delivered but the flow still executes for local/dev testing.
