# VectorDB Java 

A beginner-friendly Java implementation of a Vector Database with document ingestion, semantic search foundations, and local LLM integration using Ollama.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Build | Maven |
| LLM Runtime | Ollama |
| Embedding Model | nomic-embed-text |
| Generation Model | llama3.2 |
| Utilities | Lombok, Jackson, JUnit 5 |
| Frontend (planned) | React + Vite |

---

## Features

### Vector Operations
- Cosine Distance
- Euclidean Distance
- Manhattan Distance

### Text Processing
- Fixed-size chunking
- Configurable overlap
- Word-based tokenization

### Ollama Integration
- Embedding generation
- LLM text generation
- Availability checks
- Configurable timeouts

### Vector Store
- In-memory storage (ConcurrentHashMap)
- Insert, Delete, List
- Top-K similarity search

### Document Pipeline
```
Document Upload → Text Chunking → Embedding Generation → Vector Storage → Metadata Storage
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

### Example — Document Upload

**Request:**
```json
POST /api/documents
{
  "title": "Java Basics",
  "text": "Java is a programming language. Spring Boot is built on Java."
}
```

**Response:**
```json
{
  "chunks": 1,
  "ids": [1],
  "dims": 768
}
```

---

## Project Structure

```text
com.vectordb
├── config
├── controller
├── core
├── model
│   └── dto
├── service
└── VectorDbApplication
```

---

## Current Status

✅ Completed through **Step 6 — Document Pipeline**

### Next Milestone — Step 7: RAG Pipeline

```
Question → Query Embedding → Similarity Search → Top-K Chunks → Prompt Construction → llama3.2 → Answer
```

---

## Setup

### Prerequisites
```bash
ollama pull nomic-embed-text
ollama pull llama3.2
ollama serve
```

### Run
```bash
cd backend
mvn spring-boot:run
```

### Test
```bash
mvn test
```