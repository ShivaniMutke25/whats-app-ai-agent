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

### Architecture flow diagram

```text
WhatsApp Cloud API
      | webhook
      v
gateway-service (8081)
      | normalize + deduplicate
      v
Kafka topic whatsapp.inbound
      | consume
      v
ai-service (8084)
      | load conversation history from Redis
      | fetch customer/inventory context from context-service (8082)
      | run AI prompt orchestration and guardrails
      v
outbound-service (8083)
      | send WhatsApp reply or mock delivery
      v
WhatsApp Cloud API / local mock
```

### Why this technology stack?

- Kafka: chosen to decouple inbound webhook ingestion from AI processing and make the system resilient to spikes in message volume.
- Redis: chosen for low-latency conversation state and short-lived memory storage, supporting follow-up chat continuity without heavy persistence.
- Spring Boot / Java: provides fast service bootstrapping, validation, HTTP clients, and easy integration across multiple microservices.
- Docker Compose: simplifies local setup for a multi-service system and mirrors the distributed runtime during development.
- WhatsApp Cloud API: enables the product to live inside the customer channel where retail conversations already happen.
- OpenAI / AI fallback logic: lets the bot answer free-form retail questions while still supporting safe local development without external keys.

### Webhook technology and design

**What is a webhook?**

A webhook is a callback mechanism—a reverse API call where WhatsApp pushes events to your app instead of your app constantly asking "Do you have new messages?"

```
Traditional:  Your app polls → "Any new messages?" ← WhatsApp (every 5 seconds, wasteful)
Webhook:      WhatsApp → (HTTP POST) → Your app (real-time, when message arrives)
```

**How webhooks work in this project:**

1. **Registration**: WhatsApp is configured to send messages to `http://localhost:8081/whatsapp/webhook`
2. **Verification**: WhatsApp sends a GET request with a challenge token to verify ownership
3. **Inbound flow**: When customer sends message, WhatsApp POSTs the message payload to your webhook
4. **Processing**: `WebhookController.handleWebhook()` receives, normalizes, and publishes to Kafka
5. **Response**: Return 202 Accepted immediately; don't wait for AI to process

```
WhatsApp Cloud API
      | POST /whatsapp/webhook
      v
gateway-service (WebhookController)
      | normalize + deduplicate
      | publish event
      v
Kafka topic whatsapp.inbound
      | async consume
      v
ai-service (InboundMessageConsumer)
      | generate response
      v
outbound-service
      | send reply
      v
WhatsApp Cloud API
```

**Why webhooks instead of polling?**

| Criteria | Polling | Long Polling | WebSockets | Webhooks |
|----------|---------|--------------|-----------|----------|
| Real-time latency | ❌ 5-60s | ⚠ ~5s | ✅ <100ms | ✅ <100ms |
| Resource efficient | ❌ Wasteful | ⚠ Moderate | ✅ Good | ✅ Excellent |
| Scalability | ❌ Poor | ⚠ Moderate | ⚠ Hard | ✅ Excellent |
| Implementation | ✅ Simple | ⚠ Medium | ❌ Complex | ✅ Simple |
| WhatsApp support | ❌ None | ❌ None | ❌ None | ✅ Official |

**Polling alternative (what we avoided):**
```java
@Scheduled(fixedDelay = 5000)  // Every 5 seconds
public void pollMessages() {
    List<Message> newMessages = whatsappApi.getMessages();
    for (Message msg : newMessages) {
        inboundMessageProducer.publish(msg);
    }
}
```
Problems: 5+ second latency, bad UX, wasteful API calls, doesn't scale.

**Webhook implementation in code:**

```java
@GetMapping("/webhook")
public ResponseEntity<String> verifyWebhook(
        @RequestParam("hub.challenge") String challenge,
        @RequestParam("hub.verify_token") String verifyToken) {
    if ("local-verify-token".equals(verifyToken)) {
        return ResponseEntity.ok(challenge);  // Echo back challenge
    }
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verification failed");
}

@PostMapping("/webhook")
public ResponseEntity<Map<String, Object>> handleWebhook(@Valid @RequestBody WebhookRequest request) {
    WhatsAppMessage message = inboundMessageService.normalize(request);
    
    // Deduplicate: webhooks may retry on network failure
    if (!idempotencyService.claim(message.messageId())) {
        return ResponseEntity.accepted().body(Map.of("status", "duplicate"));
    }
    
    // Publish to Kafka for async processing
    inboundMessageProducer.publish(new WhatsAppMessageEvent(...));
    
    // Return 202 immediately (don't wait for AI response)
    return ResponseEntity.accepted().body(Map.of("status", "accepted"));
}
```

**Key webhook concepts:**

- **Idempotency**: Webhooks may be delivered multiple times. Store messageId in Redis/database to detect duplicates.
- **Async processing**: Return 202 Accepted immediately. Process the message in background via Kafka consumer.
- **Security**: Verify webhook signature via `X-Hub-Signature` header to prevent spoofing.
- **Retry logic**: If your endpoint returns error, WhatsApp retries with exponential backoff.

**Other alternatives considered:**

- **Long Polling**: Better latency than polling, but still not true real-time; requires persistent connections
- **WebSockets**: True bidirectional, but WhatsApp doesn't support; overkill for this use case
- **Message Queue**: Could have WhatsApp write to SQS/RabbitMQ, but no native WhatsApp support
- **Server-Sent Events**: Real-time push, but one-way only; WhatsApp doesn't support it

### How the design satisfies functional requirements

Functional requirements:
- Accept WhatsApp customer messages -> handled by `gateway-service` with webhook validation.
- Normalize requests and deduplicate duplicates -> performed in `gateway-service` by `InboundMessageService` and `IdempotencyService`.
- Maintain customer and inventory context -> served by `context-service` via `CustomerController` and `InventoryController`.
- Keep conversation memory for follow-up questions -> implemented in `ConversationMemoryService` using Redis.
- Generate AI responses for retail use cases -> implemented in `AIOrchestrator`, `PromptService`, and `SpringAiChatService`.
- Deliver replies through WhatsApp -> done by `OutboundServiceClient` calling `outbound-service`.
- Operate when external services are unavailable -> fallback logic in `FallbackService` and mock delivery in `MockWhatsAppClient`.

### Why this LLD design?

- The LLD separates data ingestion, business context, AI orchestration, and delivery, which is ideal for a retail bot product that may evolve into multiple conversation paths.
- The `AIOrchestrator` can be extended with new tools and guardrails without changing webhook or delivery logic.
- Using HTTP-based business context fetches keeps `ai-service` from needing direct database access to `context-service`, preserving service autonomy.
- Conversation memory in Redis is the simplest persistent layer for chat continuity and avoids the complexity of a full session database in the first iteration.
- The outbound service isolates external API retries, credential handling, and mock mode, making the AI layer testable and safe.

## Entity relationships and code-based domain model

The key entities emerge from the code:

- `WhatsAppMessageEvent` (event) is the core transport unit between `gateway-service` and `ai-service`.
- `WhatsAppMessage` maps an inbound customer message and carries `messageId`, `customerId`, `phoneNumber`, `message`, `timestamp`, and `correlationId`.
- `CustomerProfile` represents shopkeeper customer data, including `customerId`, `name`, `email`, and `phoneNumber`.
- `InventoryItem` represents shopkeeper products with `itemId`, `sku`, `name`, `description`, `quantity`, and `price`.

Entity relationships in this system:
- `WhatsAppMessageEvent` references a `customerId` and `phoneNumber`.
- `CustomerProfile` is looked up by `customerId` in `BusinessContextService`.
- `InventoryItem` data is read by `BusinessContextService` when the user asks about inventory.
- `AIOrchestrator` binds the event input, conversation history, and business context to produce `AgentResponse`.

This is effectively an event-driven domain model where customer messages and business data are linked by `customerId` and user intent rather than a traditional relational schema.

## AI workflow and AI SDLC

### AI workflow in the code

1. `InboundMessageConsumer` receives `WhatsAppMessageEvent` from Kafka.
2. It constructs a `WhatsAppMessage` domain object.
3. `AIOrchestrator.process()` validates input and safety through `InputGuardrail` and `GuardrailService`.
4. It loads recent conversation history from `ConversationMemoryService`.
5. It fetches business context from `BusinessContextService`, which calls `context-service` endpoints.
6. It builds a prompt via `PromptService` and may augment it with retrieved documents from `RagService`.
7. It invokes `SpringAiChatService` to generate a response.
8. `StructuredResponseService` normalizes the response and `OutputGuardrail` sanitizes it.
9. The response is saved back to Redis conversation memory.
10. `OutboundServiceClient` sends the reply to `outbound-service`, which delivers it to WhatsApp.

### AI SDLC considerations

- **Requirements**: Support retail order-taking, inventory queries, customer support, and delivery updates over WhatsApp.
- **Design**: Defined a microservices boundary for AI orchestration separate from ingestion and delivery.
- **Implementation**: Built guardrails, structured responses, and fallback flows to manage model safety and reliability.
- **Testing**: Local mock mode plus health-check endpoints and service-specific tests allow verification without external dependencies.
- **Deployment**: Docker Compose for local stack; environment variables isolate service URLs and credentials.
- **Monitoring**: Planned metrics for Kafka lag, Redis usage, and outbound delivery success.

### Why this approach is strong for system design interviews

- It demonstrates awareness of service boundaries, coupling, and scalability.
- It shows practical use of messaging for asynchronous workflow and retries.
- It balances product functionality with operational concerns like mock modes, fallbacks, and incremental production readiness.
- It provides a clear mapping from business requirements (orders, inventory, customers) to technical components.

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

## Response generation flow and AI orchestration

### Step-by-step response generation process

The AI service (`ai-service`) executes a 9-step pipeline before sending any response:

**Step 1: Input Validation (Guardrails)**
- `InputGuardrail.isValid()` checks message format and structure
- `GuardrailService.isSafe()` detects harmful or out-of-scope intent
- If validation fails, return safe fallback response immediately

**Step 2: Load Conversation Memory (Redis)**
- `ConversationMemoryService.load(customerId)` fetches from Redis key `whatsapp:conversation:{customerId}`
- Retrieves last 6 conversation turns (alternating user/assistant messages)
- Returns `List<ConversationTurn>` with role and content for context

**Step 3: Retrieve Business Context (REST call to context-service)**
- `BusinessContextService.buildBusinessContext(customerId, userMessage)` makes HTTP calls:
  - `GET /customers/{customerId}` → customer profile (name, email, notes)
  - `GET /inventory` → product list (name, SKU, quantity, price)
- Aggregates response into a formatted string for the prompt
- If context-service is down, includes error message without failing

**Step 4: Retrieve RAG Documents (Policy knowledge base)**
- `RagService.retrieve(userMessage)` performs keyword matching against business policies
- Hardcoded documents include:
  - "Refunds allowed within 30 days for unused items" (score 0.96)
  - "Orders can be canceled before dispatch, not after" (score 0.9)
  - "Delivery takes 3-5 business days" (score 0.88)
- Returns matching documents with relevance scores

**Step 5: Build Multi-part Prompt (Prompt assembly)**
- `PromptService.buildPrompt()` combines:
  1. System instruction
  2. Business context (customer + inventory)
  3. Conversation history
  4. Retrieved RAG documents
  5. Current customer message
- Result is a rich, contextually grounded prompt

**Step 6: Send to Chat Model (OpenAI via Spring AI)**
- `SpringAiChatService.generateResponse(systemPrompt, userPrompt)`
- Uses `ChatModel` from Spring AI framework
- If `OPENAI_API_KEY` is missing, returns safe fallback: "Configure API key to enable AI replies"
- Otherwise, calls OpenAI API with `SystemMessage` and `UserMessage`

**Step 7: Normalize Response**
- `StructuredResponseService.normalize(aiResponse, category)`
- Trims whitespace, validates non-empty content
- Returns `AgentResponse` with:
  - `answer`: sanitized text response
  - `category`: classification (ORDER, REFUND, GENERAL, etc.)
  - `confidence`: relevance score (default 0.8)
  - `requiresHumanSupport`: escalation flag

**Step 8: Output Safety Check**
- `OutputGuardrail.sanitize(normalizedAnswer)` performs final security scan
- Removes any harmful patterns or injection attempts

**Step 9: Persist and Deliver**
- Save to Redis: `ConversationMemoryService.save(customerId, userMessage, assistantResponse)` with 1-hour TTL
- Send via REST: `OutboundServiceClient.sendReply(phoneNumber, response)` to `outbound-service` endpoint `/internal/messages`

### Service architecture and file organization

All response generation logic lives in **ai-service** (port 8084):

| Component | File | Key Responsibility |
|-----------|------|---|
| **AIOrchestrator** | Main orchestrator | Calls all services in sequence, implements the 9-step pipeline |
| **PromptService** | Prompt assembly | Combines context, history, RAG docs, and customer message |
| **SpringAiChatService** | LLM integration | Calls OpenAI API with fallback for missing credentials |
| **StructuredResponseService** | Response normalization | Formats model output with category and confidence |
| **OutputGuardrail** | Output validation | Final safety check before delivery |
| **ConversationMemoryService** | Redis persistence | Loads/saves recent conversation history |
| **BusinessContextService** | Context enrichment | HTTP calls to context-service for customer and inventory data |
| **RagService** | Policy retrieval | Keyword-based matching against business knowledge base |
| **GuardrailService** + **InputGuardrail** | Input validation | Detects harmful or invalid input |
| **FallbackService** | Safety fallback | Returns safe responses when AI is unavailable |

### RAG implementation

**Current Implementation (Mock):**
```
RagService maintains hardcoded policy documents with relevance scores.
When customer asks "Can I return?", keyword matcher finds "refund" → returns matched policy.
```

**How RAG documents are used:**
1. `RagService.retrieve(userMessage)` is called in `AIOrchestrator`
2. Returns `List<RetrievedDocument>` with content and relevance score
3. Passed to `PromptService.buildPrompt()` which includes them as "Retrieved context"
4. AI model uses these policies when answering questions

**Production RAG pipeline (what you'd implement):**
- Vector database (Pinecone, Weaviate, Milvus) instead of in-memory list
- Embedding model to convert policies and questions to vectors
- Semantic similarity search instead of keyword matching
- Dynamic document management without code deployment

### Inter-microservice communication

Response generation requires HTTP calls across service boundaries:

```
ai-service (8084)
    ├─→ HTTP GET /customers/{id}
    │   └─→ context-service (8082)
    │
    ├─→ HTTP GET /inventory
    │   └─→ context-service (8082)
    │
    └─→ HTTP POST /internal/messages
        └─→ outbound-service (8083)
```

**Configuration via environment variables:**
- `CONTEXT_SERVICE_URL`: `http://context-service:8082` (default for Docker)
- `OUTBOUND_SERVICE_URL`: `http://outbound-service:8083` (default for Docker)

This avoids hardcoding and enables flexible deployment across environments.

### System-level prompts and constraints

**System Prompt** (in `AIOrchestrator`):
```
"You are a retail support assistant for small shopkeepers.
Answer clearly, reference business inventory and customer context,
and do not hallucinate details."
```

**Additional instructions** (in `PromptService`):
```
"Use the available context and customer information when relevant.
Do not invent policies or product availability.
If the answer cannot be confirmed from available context, say so clearly
and offer a safe alternative."
```

These constraints ensure the model:
- Stays grounded in business facts
- Uses provided context (inventory, customer history, policies)
- Doesn't make up information
- Falls back gracefully when uncertain

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

## How to explain this on a whiteboard

1. Start with the product problem:
   - "Retail shopkeepers need a WhatsApp-based bot for orders, inventory checks, and customer support."
2. Draw the main services:
   - `gateway-service` for webhook ingestion
   - `context-service` for customer and inventory data
   - `ai-service` for chatbot orchestration
   - `outbound-service` for message delivery
3. Draw the message flow:
   - Incoming WhatsApp webhook -> gateway -> Kafka -> AI -> outbound -> WhatsApp
4. Annotate the cross-service interactions:
   - `gateway-service` publishes `WhatsAppMessageEvent`
   - `ai-service` loads conversation history from Redis
   - `ai-service` calls `context-service` for business context
   - `ai-service` sends replies to `outbound-service`
5. Explain the technology choices briefly:
   - Kafka for async decoupling and resiliency
   - Redis for low-latency conversation state
   - Spring Boot for service development and HTTP integration
   - WhatsApp API because the product lives in the customer channel
6. Highlight production concerns:
   - durable idempotency, persistent context storage, tracing, and monitoring
7. Close with impact:
   - "This design lets shopkeepers automate sales and support while keeping the bot reliable and extensible."

## Final preparation checklist

- Practice describing the end-to-end flow in 60 seconds.
- Be ready to explain why each service exists and how they communicate.
- Emphasize the product impact for shopkeepers and retail automation.
- Mention fallback modes and why they matter during development.
- Cover production-level concerns clearly: durability, observability, retries, and scaling.
