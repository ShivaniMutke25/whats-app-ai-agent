# Interview Preparation for WhatsApp AI Agent

## Personal pitch for SDE2 interviews

I am a software developer with 5 years of experience building reliable, production-grade systems. For this project, I designed and implemented an AI-powered WhatsApp platform for retail shopkeepers, enabling them to scale order capture, deliveries, customer service, and inventory management through conversational bots.

This project showcases my ability to own end-to-end system design, build microservices, integrate external APIs, handle asynchronous workflows, and make tradeoff decisions for production readiness.

## Project summary

This solution helps shopkeepers scale their retail business by enabling customers to interact with an AI chatbot over WhatsApp. The bot can:

- take orders and confirm delivery
- answer inventory availability questions
- look up customer details and loyalty status
- surface shopkeeper inventory and order dashboards

The architecture separates ingestion, business context, AI orchestration, and delivery into independent services.

## Product problem statement

Shopkeepers often lose orders or waste time manually tracking inventory, customer details, and delivery schedules. This project reduces friction by placing an AI-enabled retail assistant directly inside WhatsApp, where customers already communicate.

The core product goals are:

- improve order conversion through conversational ordering
- reduce manual effort for order updates and delivery scheduling
- deliver faster responses to inventory and product queries
- support business workflows with contextual customer and stock data

## Core services and product responsibility

- `gateway-service` (port 8081)
  - Receives WhatsApp webhook callbacks.
  - Normalizes and validates messages.
  - Deduplicates repeated webhook events.
  - Publishes inbound events to Kafka.

- `context-service` (port 8082)
  - Stores shopkeeper data for customers and inventory.
  - Exposes APIs for product listings, customer lookup, and shopkeeper dashboards.
  - Provides the AI service with business context for replies.

- `ai-service` (port 8084)
  - Consumes normalized events from Kafka.
  - Maintains conversation memory in Redis.
  - Builds AI prompts and executes guardrails.
  - Enriches responses with inventory and customer data.
  - Sends outbound message requests to the outbound service.

- `outbound-service` (port 8083)
  - Receives internal delivery requests.
  - Sends WhatsApp messages through the WhatsApp Cloud API.
  - Falls back to mock delivery in local development.

- `common`
  - Shared domain and transport models.
  - Ensures schema compatibility across services.

## Retail bot schemes and use cases

### 1. Order capture and delivery

- Customers ask for products by name or SKU.
- Bot confirms availability using inventory data.
- Bot captures quantity, delivery address, and preferred time slot.
- Bot confirms order details and creates a delivery request.
- Follow-up flow handles order tracking and delivery status updates.

### 2. Inventory and restock assistance

- Shopkeepers can ask the bot for stock levels.
- The bot can recommend restock items based on low inventory.
- It can answer questions like "Do you have ITEM-002?" and "How many units are left?"
- This reduces manual stock lookup and improves replenishment speed.

### 3. Customer support and loyalty

- The bot can fetch customer order history and loyalty tier.
- It can answer queries about returns, refunds, or delivery delays.
- It can suggest upsell or cross-sell products based on customer preferences.
- This supports higher retention and better customer experiences.

## Architecture and design decisions

### High-level design

- Event-driven architecture with Kafka as the asynchronous boundary.
- Clear separation of concerns across services.
- Internal HTTP calls for context and outbound delivery to reduce coupling.
- Independent deployments so services can scale without affecting the entire system.

### Low-level design

- `gateway-service` handles webhook handshake, normalization, and Kafka publishing.
- `ai-service` uses a Kafka consumer group `whatsapp-ai-group` for resiliency.
- Redis stores conversation context for follow-up messages and session continuity.
- `context-service` provides business data for personalized AI responses.
- `outbound-service` abstracts WhatsApp API delivery from the AI flow.

## Production readiness and scaling

### Production improvements already identified

- Replace in-memory deduplication with Redis or database-backed idempotency.
- Migrate `context-service` to a persistent database.
- Add traceability using distributed tracing tools.
- Implement centralized logging and health monitoring.
- Harden authentication for external webhook endpoints.

### Scaling considerations

- Kafka partitions allow multiple `ai-service` consumers to share load.
- Outbound request volume can grow independently from AI processing.
- Redis can store conversation state across service instances.
- Separate service deployment supports independent scaling and fault isolation.

### Observability and reliability

- Monitor queue lag, consumer throughput, request latency, and error rates.
- Track webhook retries, deduplication metrics, and delivery success rates.
- Add health checks and readiness probes for all services.
- Use alerting for failed outbound deliveries and AI model errors.

## Candidate strengths demonstrated

- Designing microservices for a real product problem.
- Handling external API integration with WhatsApp and OpenAI.
- Building asynchronous pipelines with Kafka and Redis.
- Implementing message normalization, idempotency, and fallback modes.
- Thinking in terms of both product value and operational readiness.

## Common interview questions and strong responses

### Q1: Why did you choose microservices for this project?

A: I chose microservices because the system has distinct domains: webhook ingestion, business context, AI orchestration, and delivery. This separation lets us scale the AI engine independently from the WhatsApp gateway and delivery service, and it makes the architecture easier to evolve as new bot workflows are added.

#### Follow-up: How would you handle transactions across services?

- Use event-driven patterns rather than distributed database transactions.
- Keep each service responsible for its local data.
- Use Kafka and compensation logic for eventual consistency.
- For example, order capture can be confirmed when outbound delivery succeeds, while the AI service stores a local event if delivery fails.

### Q2: What are the biggest risks in this system?

A: The main risks are: webhook replay or duplicates, inconsistent business context, unreliable external APIs, and poor observability.

#### Follow-up: How would you mitigate these risks?

- Add durable idempotency keys in Redis or a database.
- Persist context data and use caches with expiration.
- Implement retry logic and circuit breakers for external calls.
- Add end-to-end tracing and centralized logs.

### Q3: How do you manage conversation state in the AI bot?

A: Conversation state is stored in Redis per customer. The AI service loads recent messages and context before building prompts, so follow-up questions can be answered correctly and order flow continuity is maintained.

#### Follow-up: How would you support multiple chat agents or workflows?

- Add a workflow engine or state machine to classify the current bot context.
- Use metadata in conversation state to track if the user is ordering, asking inventory, or requesting support.
- Route messages to dedicated handler logic for each scheme.

### Q4: How does the system behave without OpenAI or WhatsApp credentials?

A: The project includes fallback behavior so the core flow stays operable.
- Without `OPENAI_API_KEY`, the AI service returns a safe fallback response.
- Without WhatsApp credentials, `outbound-service` logs mock delivery.
This enables local development and demoing without requiring all external dependencies.

#### Follow-up: Why is that important?

- It improves developer productivity.
- It allows testing the architecture even when external services are unavailable.
- It prevents the entire system from failing due to missing third-party configuration.

### Q5: What production metrics would you monitor?

- Kafka lag and consumer throughput.
- Outbound delivery success and failure rates.
- Redis error rates and cache hit ratio.
- API request latency for gateway, context, AI, and outbound services.
- Webhook validation failures and duplicate event counts.

#### Follow-up: What alerts would you set?

- High Kafka consumer lag.
- Sudden increase in webhook validation errors.
- Failed outbound delivery rate above threshold.
- Redis connection errors.
- AI service response time degradation.

### Q6: How would you extend this system for 10x growth?

- Add database-backed persistence for context and orders.
- Partition Kafka topics for high throughput.
- Auto-scale AI and outbound services independently.
- Use caching and CDNs for static content if needed.
- Add a gateway API layer and rate limiting to protect downstream services.

## Interview-ready talking points

- I built this project for retail shopkeepers to automate order and delivery workflows via WhatsApp.
- I selected Kafka because it decouples the webhook and AI layers and supports asynchronous retry.
- I chose Redis for fast conversation state with low latency.
- I designed services so business logic, AI orchestration, and message delivery are isolated.
- I planned production improvements around durability, observability, and secure external integration.

## STAR-style summary for interviews

- Situation: Retail shopkeepers needed a way to scale order intake and customer interactions on WhatsApp.
- Task: Build a reliable, maintainable architecture that connects webhook ingestion, product data, AI response generation, and outbound delivery.
- Action: Implemented a microservices pipeline with Kafka for asynchronous flow, Redis for memory, and a separate outbound delivery service for WhatsApp integration.
- Result: Delivered a flexible demo system that supports order-taking, inventory questions, and customer support workflows, with clear upgrade paths for production.

## Final preparation checklist

- Practice describing the end-to-end flow in 60 seconds.
- Be ready to explain why each service exists and how they communicate.
- Emphasize the product impact for shopkeepers and retail automation.
- Mention fallback modes and why they matter during development.
- Cover production-level concerns clearly: durability, observability, retries, and scaling.
