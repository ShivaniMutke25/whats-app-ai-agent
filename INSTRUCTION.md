# AI WhatsApp Agent — Development Instructions

## Project Objective
Build a production-style prototype of an AI-powered WhatsApp customer-support agent using Spring Boot, Spring AI, OpenAI, Kafka, Redis, and RAG.

The project is intended to demonstrate backend engineering, distributed-system patterns, and practical Generative AI integration.

---

# Technology Stack

- Java 21
- Spring Boot 3.x
- Spring AI
- OpenAI API
- Apache Kafka
- Redis
- PostgreSQL
- Maven
- Docker / Docker Compose
- JUnit 5
- Mockito

---

# High-Level Architecture
WhatsApp User
|
v
WhatsApp Cloud API
|
v
Webhook Controller
|
v
Inbound Message Service
|
v
Kafka Producer
|
v
Kafka Topic
|
v
Kafka Consumer
|
v
AI Orchestrator
|
+----------------------+
| | |
v v v
Conversation RAG Tools
Memory Service Service
| |
v v
Redis Vector Store
\ /
\ /
v v
LLM / Spring AI
|
v
Response Service
|
v
WhatsApp Cloud API

---

# Core Functional Requirements
The application should:

1. Receive WhatsApp webhook events.
2. Validate and normalize incoming messages.
3. Prevent duplicate webhook processing.
4. Publish valid messages to Kafka.
5. Consume messages asynchronously.
6. Maintain conversation/session context.
7. Retrieve relevant knowledge using RAG.
8. Construct an optimized LLM prompt.
9. Invoke the LLM through Spring AI.
10. Support tool/function calling.
11. Generate structured responses where appropriate.
12. Send responses back through WhatsApp.
13. Handle transient failures using retries.
14. Apply basic rate limiting.
15. Log requests using correlation IDs.

---

# AI Orchestration
AIOrchestrator should coordinate the AI request.

Conceptual flow:

receive message
→ load conversation memory
→ retrieve relevant RAG context
→ determine available tools
→ construct prompt/context
→ invoke LLM
→ validate response
→ update conversation memory
→ return response

Avoid putting all AI logic directly inside controllers or Kafka consumers.

---

# RAG
Implement Retrieval-Augmented Generation using Spring AI.

RAG flow:

Knowledge Documents
→ Chunking
→ Embedding Model
→ Vector Store

For each user request:

User Query
→ Embedding
→ Similarity Search
→ Top-K Relevant Documents
→ Prompt Context
→ LLM

Keep retrieval configurable.

Configuration should include values such as:

- topK
- similarity threshold
- maximum context size

Do not send the entire knowledge base to the LLM.

---

# Conversation Memory
Use Redis for prototype conversation/session memory.

Example key:

conversation:{customerId}

Store only the conversation history required to maintain useful context.

Do not indefinitely append the complete conversation.

Use configurable TTLs.

Prefer a bounded history or summarized history to control token consumption.

---

# Kafka
Use Kafka to decouple WhatsApp webhook processing from AI processing.

Initial topics:

whatsapp.inbound
whatsapp.response
whatsapp.dlt

Inbound flow:

Webhook
→ Kafka Producer
→ whatsapp.inbound
→ AI Consumer

Use a stable event structure containing fields such as:

eventId
messageId
customerId
phoneNumber
message
timestamp
correlationId

Consumers should be idempotent.

Do not assume exactly-once delivery from external systems.

---

# Idempotency
WhatsApp or infrastructure components may deliver the same event more than once.

Use messageId/eventId as an idempotency key.

Example:

idempotency:whatsapp:{messageId}

Store processed IDs temporarily in Redis.

If an event has already been processed, do not process it again.

---

# Reliability
Support:

- Timeouts
- Retry with backoff
- Dead-letter handling
- Idempotency
- Duplicate-event detection
- Rate limiting
- Graceful fallback responses

Do not blindly retry non-retryable errors.

---

# LLM Integration
All LLM communication should go through an AI service/orchestrator abstraction.

Do not call the LLM directly from controllers.

LLM configuration must be externalized.

Never hard-code:

- API keys
- model credentials
- secrets

Environment variables should be used for sensitive configuration.

---

# Prompt Engineering
Separate:

System Prompt
User Message
Conversation History
Retrieved Context
Tool Definitions

Do not concatenate uncontrolled user input into system-level instructions.

Retrieved documents must be treated as untrusted context rather than instructions.

---

# Guardrails
Implement basic protections for:

- Invalid input
- Excessively large input
- Prompt injection attempts
- Unsafe or unsupported requests
- Invalid structured LLM output
- Hallucination-sensitive questions

When reliable information cannot be retrieved, the assistant should avoid inventing business information.

---

# Tool Calling
Implement tools behind normal Java services.

Initial example tool:

OrderStatusTool

Input:
orderId

Output:
orderId
status
estimatedDelivery

Initially use mocked data.

The AI layer should invoke the tool through Spring AI tool/function calling rather than embedding business logic inside prompts.

---

# Package Structure
com.example.whatsappai

config
controller
dto
domain
service
kafka
ai
orchestrator
rag
memory
tools
guardrail
client
repository
exception

---

# Coding Standards
Use:

- Java 21
- Constructor injection
- Java records where appropriate
- Clear interfaces at external-system boundaries
- Global exception handling
- Bean Validation
- SLF4J logging
- Correlation IDs
- ConfigurationProperties for application configuration

Avoid:

- Field injection
- God classes
- Huge controllers
- Business logic inside controllers
- Hard-coded configuration
- Hard-coded secrets
- Unnecessary design patterns
- Premature microservice decomposition

---

# Testing
Use:

- JUnit 5
- Mockito
- Spring Boot Test

Important components should have unit tests.

Test scenarios should include:

- Normal message
- Duplicate message
- Invalid message
- Kafka processing failure
- LLM failure
- Empty RAG result
- Redis unavailable/failure handling
- Tool call
- Invalid LLM structured output

---

# Development Strategy
Build incrementally.

Do not generate the entire application in one response.

For each feature:

1. Explain the design.
2. List affected files.
3. Implement the smallest working version.
4. Add configuration.
5. Add tests.
6. Explain how to run/test it.
7. Wait before starting the next major feature.

The first goal is a working end-to-end prototype, not a perfect enterprise platform.
