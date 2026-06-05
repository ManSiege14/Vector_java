// src/main/java/com/vectordb/controller/DocumentController.java
package com.vectordb.controller;

import com.vectordb.model.dto.request.InsertDocumentRequest;
import com.vectordb.model.dto.response.DocumentListResponse;
import com.vectordb.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * POST /api/documents
     * Body: { "title": "...", "text": "..." }
     *
     * 400 — title or text missing
     * 503 — Ollama unreachable
     * 200 — { chunks, ids, dims }
     */
    @PostMapping
    public ResponseEntity<?> insertDocument(@RequestBody InsertDocumentRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "title is required"));
        }
        if (request.getText() == null || request.getText().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "text is required"));
        }

        List<Integer> ids = documentService.insertDocument(request);

        if (ids.isEmpty()) {
            return ResponseEntity.status(503)
                    .body(Map.of(
                            "error",
                            "Ollama unavailable. Install from https://ollama.com " +
                            "then run: ollama pull nomic-embed-text"
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "chunks", ids.size(),
                "ids",    ids,
                "dims",   768
        ));
    }

    /**
     * GET /api/documents
     * Returns all stored chunks with preview and metadata.
     */
    @GetMapping
    public List<DocumentListResponse> listDocuments() {
        return documentService.listAll();
    }

    /**
     * DELETE /api/documents/{id}
     * Removes one chunk by vector ID.
     * 404 if ID does not exist in either store.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable int id) {
        boolean deleted = documentService.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("ok", true));
    }
}