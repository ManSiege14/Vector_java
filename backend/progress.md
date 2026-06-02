# VectorDB Progress Tracker

Java Spring Boot + React  vector database with RAG pipeline.

---

## Project Overview

| Item | Detail |
|---|---|
| Original | C++ single-file server (`main.cpp`) with embedded HNSW, KD-Tree, Brute Force |
| Rewrite target | Java 21 + Spring Boot 3.x backend, React + Vite frontend |
| Vector search | In-memory brute-force first, `hnswlib` later |
| LLM integration | Ollama (local) — `nomic-embed-text` + `llama3.2` |
| Base package | `com.vectordb` |

---

## Package Structure

```
backend/
└── src/
    ├── main/java/com/vectordb/
    │   ├── VectorDbApplication.java
    │   ├── core/
    │   │   ├── VectorMath.java
    │   │   └── TextChunker.java
    │   ├── config/          (Step 5+)
    │   ├── controller/      (Step 4+)
    │   ├── model/           (Step 5+)
    │   └── service/         (Step 4+)
    └── test/java/com/vectordb/
        └── core/
            ├── VectorMathTest.java
            └── TextChunkerTest.java
```

---

## Completed Steps

---

### ✅ Step 1 — Project Skeleton

**Files:**

| File | Purpose |
|---|---|
| `pom.xml` | Maven build — Spring Boot 3.2.5, Java 21, Lombok, JUnit 5 |
| `VectorDbApplication.java` | Spring Boot entry point (`@SpringBootApplication`) |
| `application.properties` | Server port (8080), Ollama URL, model names, vector config |

**Verify:**
```bash
cd backend
mvn spring-boot:run
curl http://localhost:8080   # expect 404 JSON (no endpoints yet)
```

---

### ✅ Step 2 — Vector Math Utilities

**Files:**

| File | Purpose |
|---|---|
| `core/VectorMath.java` | Pure static utility — distance calculations |
| `core/VectorMathTest.java` | 24 unit tests |

**Public API:**

```java
VectorMath.cosineDistance(double[] a, double[] b)     // 0.0 = identical
VectorMath.cosineSimilarity(double[] a, double[] b)   // 1.0 = identical
VectorMath.euclideanDistance(double[] a, double[] b)
VectorMath.manhattanDistance(double[] a, double[] b)
VectorMath.distance(double[] a, double[] b, String metric) // "cosine"|"euclidean"|"manhattan"
VectorMath.toArray(List<Double> list)
VectorMath.toList(double[] arr)
VectorMath.validateSameLength(double[] a, double[] b)
VectorMath.validateDimensions(double[] v, int expected)
```

**Verify:**
```bash
mvn test -Dtest=VectorMathTest
# expect: Tests run: 24, Failures: 0, Errors: 0
```

---

### ✅ Step 3 — Text Chunker

**Files:**

| File | Purpose |
|---|---|
| `core/TextChunker.java` | Pure static utility — splits text into overlapping word windows |
| `core/TextChunkerTest.java` | 20 unit tests covering edge cases |

**Public API:**

```java
TextChunker.chunk(String text)                                    // uses defaults: 250 words, 30 overlap
TextChunker.chunk(String text, int chunkWords, int overlapWords)  // explicit sizes
TextChunker.wordCount(String text)                                // used by DocumentService for /doc/list preview

TextChunker.DEFAULT_CHUNK_WORDS   = 250
TextChunker.DEFAULT_OVERLAP_WORDS = 30
```

**Algorithm:** tokenize by whitespace → step at `(chunkWords - overlapWords)` intervals → rejoin with single space. Direct port of `chunkText()` from original `main.cpp`.

**Edge cases handled:** null/blank input, text shorter than chunk size, exact chunk size boundary, zero overlap, whitespace normalization (tabs/newlines/multiple spaces), invalid parameter guards.

**Verify:**
```bash
mvn test -Dtest=TextChunkerTest
# expect: Tests run: 20, Failures: 0, Errors: 0
```

**Run all tests so far:**
```bash
mvn test
# expect: Tests run: 44, Failures: 0, Errors: 0
```

---

## Pending Steps

---

### ⬜ Step 4 — Ollama Integration + Status Endpoint  ← NEXT

**Files to create:**

| File | Purpose |
|---|---|
| `service/OllamaService.java` | HTTP client wrapping Ollama `/api/embeddings` and `/api/generate` |
| `controller/StatusController.java` | `GET /status` endpoint |
| `model/dto/response/StatusResponse.java` | Response DTO |

**What it will do:**
- `OllamaService.embed(String text)` → `double[]` — calls `/api/embeddings` with `nomic-embed-text`
- `OllamaService.generate(String prompt)` → `String` — calls `/api/generate` with `llama3.2`, `stream: false`
- `OllamaService.isAvailable()` → `boolean` — HEAD or GET `/api/tags`
- `GET /status` returns: `ollamaAvailable`, `embedModel`, `genModel`, `docCount`, `docDims`, `demoCount`
- Connection timeout: 2s, read timeout: 30s (embed), 180s (generate)

---

### ⬜ Step 5 — Vector Store + Demo Endpoints

**Files to create:**

| File | Purpose |
|---|---|
| `service/VectorStoreService.java` | In-memory store + brute-force search via `VectorMath` |
| `service/DemoSeederService.java` | Seeds 20 hardcoded 16D vectors on startup |
| `controller/DemoController.java` | REST endpoints for demo vector operations |
| `config/AppConfig.java` | Spring beans, `RestTemplate` |
| `model/VectorItem.java` | Entity: `id`, `metadata`, `category`, `double[]` embedding |
| DTOs | `InsertRequest`, `SearchRequest`, `SearchResult`, `BenchmarkResult` |

**Endpoints:** `GET /items`, `POST /insert`, `DELETE /delete/{id}`, `GET /search`, `GET /benchmark`, `GET /hnsw-info` (stub), `GET /stats`

---

### ⬜ Step 6 — Document Pipeline

**Files to create:**

| File | Purpose |
|---|---|
| `service/DocumentService.java` | Chunk → embed each chunk → store in `VectorStoreService` (768D instance) |
| `controller/DocumentController.java` | REST endpoints for document CRUD |
| `model/DocItem.java` | Entity: `id`, `title`, `text`, `double[]` embedding |
| DTOs | `InsertDocRequest`, `DocListItem`, `DocInsertResponse` |

**Endpoints:** `POST /doc/insert`, `GET /doc/list`, `DELETE /doc/delete/{id}`

---

### ⬜ Step 7 — RAG Pipeline

**Files to create:**

| File | Purpose |
|---|---|
| `service/RagService.java` | Retrieve top-k chunks → build prompt → call `OllamaService.generate` |
| `controller/RagController.java` | REST endpoints for RAG |
| DTOs | `AskRequest`, `AskResponse`, `ContextChunk` |

**Endpoints:** `POST /doc/search`, `POST /doc/ask`

**Behaviour:** embed question → cosine search → filter results above distance 0.7 → build prompt matching original C++ template → return answer + context chunks used.

---

### ⬜ Step 8 — CORS + React Frontend Scaffold

**Files to create:**

| File | Purpose |
|---|---|
| `config/CorsConfig.java` | Allow `localhost:5173` → `localhost:8080` |
| `frontend/` | Vite + React project scaffold |
| `frontend/src/api/client.js` | Centralised fetch wrappers for all endpoints |

---

### ⬜ Step 9 — React Components

| Component | Purpose |
|---|---|
| `StatusBar.jsx` | Header — Ollama status badge, vector count |
| `SearchTab.jsx` | Query box, algorithm selector, metric selector, k slider, results list |
| `DocumentsTab.jsx` | Document insert form, stored document list |
| `AskAITab.jsx` | Question input, streamed answer display, context chip expanders |
| `ScatterPlot.jsx` | 2D PCA canvas — semantic space visualisation |
| `useSearch.js` | Custom hook for demo search state |
| `useDocuments.js` | Custom hook for document CRUD state |
| `useStatus.js` | Custom hook for polling `/status` |

---

### ⬜ Step 10 — Polish + Error Handling

- Input validation on all endpoints (400 responses)
- Graceful error responses when Ollama is offline
- Loading states and error boundaries in React
- `/benchmark` full implementation (time all three strategies)
- `/stats` endpoint

---

## Future Improvements (Post-MVP)

| Feature | Notes |
|---|---|
| HNSW index | Replace brute force with `hnswlib-java` |
| Persistence | SQLite via JDBC — documents survive restart |
| Streaming responses | SSE for LLM token streaming |
| Configurable similarity threshold | Currently hardcoded at 0.7 |
| Multiple embedding models | Currently single model only |
| Docker setup | `docker-compose.yml` for full stack |

---

## Architecture Decisions

| Decision | Reason |
|---|---|
| Static utility classes (`VectorMath`, `TextChunker`) | No Spring dependency, fully unit-testable in isolation; mirrors original C++ free-function design |
| `double[]` internally, `List<Double>` at API boundary | Performance inside the store, Jackson-serialisable outside |
| Single `VectorStoreService` class, two instances | Same storage + search logic reused for 16D demo vectors and 768D document embeddings |
| Brute-force search first | Correct for all three metrics, trivial to test, fast enough under ~10k vectors; HNSW added later without changing the service interface |
| Overlap chunking matches C++ original | `step = chunkWords - overlapWords`; last N words of chunk K are first N words of chunk K+1 — preserves context across boundaries |
| `application.properties` for all config | Ollama URL, model names, timeouts — no hardcoded values anywhere in service code |
| No hand-rolled JSON | Replaced entirely by Jackson via Spring Boot auto-configuration |

---

## Quick Reference

### Running the backend
```bash
cd backend
mvn spring-boot:run
```

### Running tests
```bash
mvn test                                  # all tests
mvn test -Dtest=VectorMathTest            # one class
mvn test -Dtest=TextChunkerTest           # one class
mvn test -Dtest=VectorMathTest#identicalVectors   # one method
```

### Running the frontend (once created — Step 8)
```bash
cd frontend
npm install
npm run dev       # http://localhost:5173
```

### API base URL
```
http://localhost:8080
```

### Ollama setup (prerequisite)
```bash
ollama pull nomic-embed-text   # 274 MB — embedding model
ollama pull llama3.2           # ~2 GB — generation model
ollama serve                   # if not already running
```