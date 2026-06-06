# VectorDB Java

A from-scratch Java implementation of a Vector Database and Retrieval-Augmented Generation (RAG) system built using Spring Boot and Ollama.

The goal of this project is to understand how modern AI systems work internally by implementing the core mechanics manually — no LangChain, no Spring AI.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Build | Maven |
| Utilities | Lombok, Jackson, Jakarta Validation |
| LLM Runtime | Ollama |
| Embedding Model | nomic-embed-text |
| Generation Model | llama3.2 |
| Frontend (planned) | React + Vite |

---

## Features

### Vector Engine
- Cosine, Euclidean, Manhattan distance
- Exact KNN search
- In-memory vector storage (ConcurrentHashMap)

### Text Processing
- Word-based tokenization
- Configurable chunk size and overlap
- Chunk metadata tracking

### Ollama Integration
- Embedding generation via nomic-embed-text
- Text generation via llama3.2
- Availability checks with dedicated timeout configurations

### Document Ingestion
- Upload raw text documents
- Automatic chunking → embedding → vector storage → metadata storage

### Retrieval-Augmented Generation (RAG)
- Query embedding
- Similarity search
- Context retrieval
- Prompt construction
- Local answer generation via llama3.2

---

## Architecture

```
Document Upload
    → TextChunker
    → Embedding Generation
    → Vector Store
    → Metadata Store

User Question
    → Query Embedding
    → Similarity Search
    → Context Retrieval
    → Prompt Builder
    → llama3.2
    → Final Answer
```

---

## API Endpoints

### System
```
GET  /status
```

### Demo Vector Store
```
GET    /api/demo/items
POST   /api/demo/insert
POST   /api/demo/search
DELETE /api/demo/delete/{id}
```

### Document Pipeline
```
POST   /api/documents
GET    /api/documents
DELETE /api/documents/{id}
```

### RAG Pipeline
```
POST   /api/rag/ask
```

**Request:**
```json
{
  "question": "How does binary search work?",
  "k": 3
}
```

**Response:**
```json
{
  "question": "How does binary search work?",
  "answer": "...",
  "retrievedChunks": [...],
  "chunkCount": 3
}
```

---

## Current Capabilities

| Capability | Status |
|-----------|--------|
| Upload documents | ✅ |
| Chunk documents | ✅ |
| Generate embeddings | ✅ |
| Store vectors | ✅ |
| Search vectors | ✅ |
| Retrieve relevant context | ✅ |
| Generate grounded answers | ✅ |

---

## Current Limitations

- Linear O(N × D) exact search — no ANN indexing
- In-memory storage only — no persistence
- No HNSW / IVF indexing
- No authentication
- Single-node architecture

---

## Future Improvements

- HNSW Approximate Nearest Neighbor Search
- Persistent Storage Layer
- Async Embedding Pipeline
- Document Re-ranking
- Hybrid Search (Keyword + Vector)
- Streaming Responses
- Web UI

---

## Setup

```bash
# Pull Ollama models
ollama pull nomic-embed-text
ollama pull llama3.2
ollama serve

# Run backend
cd backend
mvn spring-boot:run

# Run tests
mvn test
```

---

## Learning Objectives

This project demonstrates understanding of:

- Vector Databases and Embedding Models
- Similarity Search and KNN
- Retrieval-Augmented Generation
- Spring Boot Architecture and REST API Design
- Local LLM Integration
- Concurrent Data Structures

---

## Project Status

**Current Milestone:** ✅ Step 7 Complete — Retrieval-Augmented Generation Pipeline

**Next Milestone:** Step 8 — Advanced Retrieval and Production Improvements