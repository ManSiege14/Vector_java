# VectorDB Java

> A from-scratch Retrieval-Augmented Generation (RAG) system built with Java, Spring Boot, React, and Ollama.

No LangChain. No Spring AI. No vector database libraries.

This project implements the core building blocks of a modern AI retrieval system manually, in order to understand how production RAG pipelines work internally.

---

## Overview

```
Upload PDF
   │
   ▼
Extract Text
   │
   ▼
Chunk Document
   │
   ▼
Generate Embeddings
   │
   ▼
Store Vectors
   │
   ▼
Semantic Search
   │
   ▼
RAG Answer
```

---

## Why This Project?

Modern AI applications rely on Retrieval-Augmented Generation (RAG) to answer questions over private knowledge.

Instead of using frameworks such as LangChhain or Spring AI, this project rebuilds the complete pipeline manually:

- Document ingestion
- Chunking
- Embedding generation
- Vector similarity search
- Prompt construction
- Local LLM inference
- Persistence

The objective was to understand how each component works internally rather than treating the system as a black box.

---

## Features

### AI Pipeline
- PDF Upload
- Plain Text Upload
- Automatic Chunking
- Local Embedding Generation
- Semantic Search
- Retrieval-Augmented Generation
- Local LLM Responses

### Vector Database
- Custom Vector Store
- Cosine Distance
- Euclidean Distance
- Manhattan Distance
- Exact KNN Search

### Frontend
- React + Vite
- Semantic Search Interface
- Document Browser
- Ask AI Interface
- PDF Upload
- System Status Dashboard

### Persistence
- `vectors.json`
- `documents.json`
- Automatic Save
- Startup Recovery

---

## Architecture

```
                   React Frontend
                          │
──────────────────────────┼──────────────────────────
                          │
                 Spring Boot REST API
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
DocumentController   DemoController   RagController
        │                                 │
        ▼                                 ▼
DocumentService                   RagService
        │                                 │
        ▼                                 ▼
 TextChunker                  Ollama Embedding
        │                                 │
        ▼                                 ▼
 VectorStoreService  ◄──────── Similarity Search
        │
        ▼
 PersistenceService
        │
        ▼
vectors.json
documents.json
```

---

## Technology Stack

| Layer       | Technology       |
| ----------- | ---------------- |
| Language    | Java 21          |
| Backend     | Spring Boot 3.2  |
| Frontend    | React + Vite     |
| Build       | Maven            |
| AI Runtime  | Ollama           |
| Embeddings  | nomic-embed-text |
| LLM         | llama3.2         |
| PDF         | Apache PDFBox    |
| Persistence | Jackson JSON     |
| HTTP        | REST APIs        |

---

## Project Structure

```
backend
│
├── config
├── controller
├── core
├── model
│
├── service
│      DocumentService
│      RagService
│      OllamaService
│      PersistenceService
│      PdfService
│      VectorStoreService
│
└── resources

frontend
│
├── pages
│      Search
│      Documents
│      Ask AI
│
├── api.js
└── App.jsx
```

---

## System Workflow

```
PDF
 │
 ▼
PdfService
 │
 ▼
TextChunker
 │
 ▼
Ollama Embeddings
 │
 ▼
Vector Store
 │
 ├────────► Search Page
 │
 ▼
RagService
 │
 ▼
Prompt Builder
 │
 ▼
llama3.2
 │
 ▼
Answer
```

---

## REST API

| Method | Endpoint                | Description                          |
| ------ | ------------------------ | ------------------------------------- |
| POST   | `/api/documents/upload`  | Upload a PDF or text document         |
| GET    | `/api/documents`         | List all stored documents             |
| GET    | `/api/search/text`       | Semantic search over stored chunks    |
| POST   | `/api/rag/ask`           | Ask a question using the RAG pipeline |

---

## Running the Project

### Prerequisites
- Java 21
- Maven
- Node.js + npm
- [Ollama](https://ollama.com) running locally with `nomic-embed-text` and `llama3.2` pulled

### Backend

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The backend serves the REST API; the frontend runs on Vite's dev server and consumes it.

---

## Current Capabilities

- ✅ PDF Upload
- ✅ TXT Upload
- ✅ Automatic Chunking
- ✅ Local Embeddings
- ✅ Semantic Search
- ✅ Retrieval-Augmented Generation
- ✅ React Frontend
- ✅ Startup Recovery
- ✅ Persistent Vector Storage
- ✅ REST APIs

---

## Future Roadmap

**Near-term**
- Step 13 — Retrieval Scores
- Step 14 — Grouped Document Management
- Step 15 — UI Polish
- Step 16 — Conversation Memory

**Future**
- Docker
- PostgreSQL + pgvector
- Hybrid Search
- HNSW
- Streaming Responses
- Authentication

---

## Learning Outcomes

Building this project provided hands-on experience with:

- Vector Databases
- Retrieval-Augmented Generation
- Embedding Models
- Similarity Search
- Spring Boot REST APIs
- React Frontend Development
- PDF Processing
- Local LLM Integration
- Persistence Design
- Concurrent Data Structures

---
