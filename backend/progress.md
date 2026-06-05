# VectorDB Progress Tracker

Java 21 + Spring Boot 3.x backend, React + Vite frontend (planned). Local LLM via Ollama.

---

## Completed Steps

---

### ✅ Step 1 — Spring Boot Foundation

**Files:** `pom.xml`, `VectorDbApplication.java`, `application.properties`

- Maven project setup, Spring Boot entry point, base configuration

**Verify:**
```bash
mvn spring-boot:run
curl http://localhost:8080/status
```

---

### ✅ Step 2 — Vector Math Engine

**Files:** `core/VectorMath.java`, `core/VectorMathTest.java`

**Public API:**
```java
VectorMath.cosineDistance(double[] a, double[] b)
VectorMath.cosineSimilarity(double[] a, double[] b)
VectorMath.euclideanDistance(double[] a, double[] b)
VectorMath.manhattanDistance(double[] a, double[] b)
VectorMath.distance(double[] a, double[] b, String metric)  // "cosine"|"euclidean"|"manhattan"
VectorMath.toArray(List<Double> list)
VectorMath.toList(double[] arr)
```

**Verify:** `mvn test -Dtest=VectorMathTest` → 24 tests pass

---

### ✅ Step 3 — Text Chunker

**Files:** `core/TextChunker.java`, `core/TextChunkerTest.java`

**Public API:**
```java
TextChunker.chunk(String text)                                    // defaults: 250 words, 30 overlap
TextChunker.chunk(String text, int chunkWords, int overlapWords)
TextChunker.wordCount(String text)
```

**Verify:** `mvn test -Dtest=TextChunkerTest` → 20 tests pass

---

### ✅ Step 4 — Ollama Integration

**Files:** `service/OllamaService.java`, `controller/StatusController.java`, `model/dto/response/StatusResponse.java`

**Public API:**
```java
OllamaService.embed(String text)       // → double[] via nomic-embed-text
OllamaService.generate(String prompt)  // → String via llama3.2
OllamaService.isAvailable()            // → boolean, 2s timeout
```

**Endpoint:** `GET /status`

**Verify:**
```bash
curl http://localhost:8080/status
# { "ollamaAvailable": true, "embedModel": "nomic-embed-text", "genModel": "llama3.2", "docCount": 0, "demoCount": 0 }
```

---

### ✅ Step 5A — Vector Store

**Files:** `config/AppConfig.java`, `model/VectorItem.java`, `service/VectorStoreService.java`

**Public API:**
```java
store.insert(VectorItem item)
store.delete(String id)
store.list()                                           // → List<VectorItem>
store.search(double[] query, int k, String metric)     // → List<SearchResult>, sorted by score
```

Storage: ConcurrentHashMap · Metrics: cosine, euclidean, manhattan · Top-K retrieval

---

### ✅ Step 5B — Demo API

**Files:** `service/DemoSeederService.java`, `controller/DemoController.java` + DTOs

**Endpoints:**
```
GET    /api/demo/items
POST   /api/demo/insert
POST   /api/demo/search
DELETE /api/demo/delete/{id}
```

Demo dataset: 20 seeded 16D vectors · categories: `cs`, `math`, `food`, `sports`

**Verify:**
```bash
curl http://localhost:8080/api/demo/items
```

---

### ✅ Step 6 — Document Pipeline

**Files:** `model/DocItem.java`, `model/dto/InsertDocumentRequest.java`, `model/dto/response/DocumentListResponse.java`, `service/DocumentService.java`, `controller/DocumentController.java`

**Pipeline:**
```
Document → TextChunker → OllamaService.embed() → VectorStoreService.insert() → Metadata Store
```

**Endpoints:**
```
POST   /api/documents
GET    /api/documents
DELETE /api/documents/{id}
```

**Verified:**
- Document upload and chunk generation
- Embedding creation (768D via nomic-embed-text)
- Storage and retrieval
- Ollama failure handling

---

## Current Package Structure

```
com.vectordb
├── config          → AppConfig
├── controller      → StatusController, DemoController, DocumentController
├── core            → VectorMath, TextChunker
├── model
│   ├── DocItem, VectorItem
│   └── dto/        → InsertDocumentRequest, InsertVectorRequest, SearchRequest,
│       response/     SearchResult, SearchResponse, StatusResponse, DocumentListResponse
├── service         → OllamaService, VectorStoreService, DemoSeederService, DocumentService
└── VectorDbApplication
```

### Existing Files (do not regenerate)
`VectorDbApplication.java`, `VectorMath.java`, `VectorMathTest.java`, `TextChunker.java`, `TextChunkerTest.java`, `OllamaService.java`, `StatusController.java`, `StatusResponse.java`, `VectorStoreService.java`, `VectorItem.java`, `AppConfig.java`, `DemoSeederService.java`, `DemoController.java`, `DocItem.java`, `InsertDocumentRequest.java`, `DocumentListResponse.java`, `DocumentService.java`, `DocumentController.java`, `application.properties`, `pom.xml`

---

## ➡️ Next Step — Step 7: RAG Pipeline

**Goal:** Embed a question, retrieve relevant chunks, build a prompt, generate an answer.

**Pipeline:**
```
Question → Embed Query → Search Similar Chunks → Retrieve Context → Build Prompt → llama3.2 → Answer
```

**Files to create:**

| File | Purpose |
|------|---------|
| `model/dto/RagRequest.java` | Request DTO: question string |
| `model/dto/response/RagResponse.java` | Response DTO: answer + context chunks used |
| `service/RagService.java` | Orchestrates retrieval + prompt building + generation |
| `controller/RagController.java` | REST endpoint |

**Target endpoint:** `POST /api/rag/ask`

---

## Roadmap

| Step | Status |
|------|--------|
| 1 — Spring Boot Setup | ✅ |
| 2 — VectorMath | ✅ |
| 3 — TextChunker | ✅ |
| 4 — Ollama Integration | ✅ |
| 5 — Vector Store + Demo API | ✅ |
| 6 — Document Pipeline | ✅ |
| 7 — RAG Pipeline | ➡️ Next |
| 8 — React Frontend | ⬜ |
| 9 — Polish + Error Handling | ⬜ |
| Future — HNSW, Persistence, Docker | ⬜ |

---

## Architecture Decisions

| Decision | Reason |
|----------|--------|
| Static utility classes (`VectorMath`, `TextChunker`) | No Spring dependency; fully unit-testable; mirrors original C++ design |
| `double[]` internally, `List<Double>` at API boundary | Performance inside store, Jackson-serialisable outside |
| Single `VectorStoreService`, two instances | Same logic reused for 16D demo and 768D document embeddings |
| Embedding dims discovered dynamically | Not hardcoded; resolved from first Ollama response |
| Brute-force search first | Correct for all metrics; fast under ~10k vectors; HNSW added later without changing service interface |
| `application.properties` for all config | No hardcoded values in service code |

---

## Quick Reference

```bash
# Run backend
cd backend && mvn spring-boot:run

# Run all tests
mvn test

# Ollama setup
ollama pull nomic-embed-text
ollama pull llama3.2
ollama serve
```