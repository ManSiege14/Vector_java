# VectorDB Java 

A Java Vector Database + RAG (Retrieval-Augmented Generation) system.

## Tech Stack

* Java 21
* Spring Boot 3.x
* Maven
* JUnit 5
* Ollama
* React + Vite (planned)

## Goal

Build a Vector Database + RAG System while learning:

* Backend Engineering
* Java Fundamentals
* Spring Boot
* REST APIs
* Vector Search
* Testing with JUnit
* AI System Architecture
* Retrieval-Augmented Generation (RAG)

---

## Project Status

### ✅ Step 1 — Spring Boot Project Setup

Implemented:

* Maven project configuration
* Spring Boot application entry point
* Application configuration

Files:

* `pom.xml`
* `VectorDbApplication.java`
* `application.properties`

Verified:

```bash
mvn spring-boot:run
```

Backend starts successfully on port 8080.

---

### ✅ Step 2 — Vector Math Utilities

Implemented:

* Cosine Similarity
* Cosine Distance
* Euclidean Distance
* Manhattan Distance
* Vector validation utilities
* Array/List conversion helpers

Files:

* `core/VectorMath.java`
* `core/VectorMathTest.java`

Verified:

```bash
mvn test -Dtest=VectorMathTest
```

---

### ✅ Step 3 — Text Chunking Engine

Implemented:

* Overlapping text chunking
* Configurable chunk size
* Configurable overlap size
* Word counting utility
* Input validation
* Edge case handling

Files:

* `core/TextChunker.java`
* `core/TextChunkerTest.java`

Default configuration:

```text
Chunk Size: 250 words
Overlap: 30 words
```

Verified:

```bash
mvn test -Dtest=TextChunkerTest
```

---

## Current Architecture

```text
backend/
└── src/
    ├── main/java/com/vectordb/
    │   ├── VectorDbApplication.java
    │   └── core/
    │       ├── VectorMath.java
    │       └── TextChunker.java
    │
    └── test/java/com/vectordb/
        └── core/
            ├── VectorMathTest.java
            └── TextChunkerTest.java
```

---

### ✅Step 4 — Ollama Integration
Implemented:

OllamaService
StatusController
StatusResponse DTO
/status endpoint

Features:

Ollama availability check
Embedding generation support
LLM text generation support
Model status reporting

Example Response:

{
  "ollamaAvailable": true,
  "embedModel": "nomic-embed-text",
  "genModel": "llama3.2",
  "docCount": 0,
  "docDims": 0,
  "demoCount": 0
}

## Long-Term Roadmap

* Vector Store Service
* Demo Search API
* Document Pipeline
* RAG Pipeline
* React Frontend
* HNSW Integration
* Persistence Layer
* Performance Benchmarking

---

## Learning Notes

This project is intentionally being built step-by-step.

Focus areas:

* Understanding every class before moving forward
* Writing and running tests for each module
* Learning Spring Boot fundamentals through implementation
* Understanding vector search and RAG internals instead of only using frameworks
