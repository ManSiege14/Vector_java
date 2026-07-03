// src/main/java/com/vectordb/service/DocumentService.java
package com.vectordb.service;

import com.vectordb.core.TextChunker;
import com.vectordb.model.DocItem;
import com.vectordb.model.VectorItem;
import com.vectordb.model.dto.request.InsertDocumentRequest;
import com.vectordb.model.dto.response.DocumentListResponse;
import com.vectordb.model.dto.response.DocumentSummaryResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocumentService {

    private final OllamaService ollamaService;
    private final VectorStoreService docStore;
    private final PersistenceService persistenceService;

    // Parallel metadata store: vectorId -> DocItem
    private final Map<Integer, DocItem> metadataStore = new ConcurrentHashMap<>();

    public DocumentService(
            OllamaService ollamaService,
            @Qualifier("docVectorStore") VectorStoreService docStore,
            PersistenceService persistenceService) {

        this.ollamaService = ollamaService;
        this.docStore = docStore;
        this.persistenceService = persistenceService;
    }

    /**
     * PostConstruct initialization hook to sync in-memory stores with local disk snapshots
     * right when the bean is spun up by Spring's ApplicationContext.
     */
    @PostConstruct
    public void loadPersistedData() {
        try {
            Map<Integer, VectorItem> vectors = persistenceService.loadVectors();
            Map<Integer, DocItem> documents = persistenceService.loadDocuments();

            docStore.loadStore(vectors);

            metadataStore.clear();
            metadataStore.putAll(documents);

            log.info("Loaded {} vectors and {} documents from disk",
                    vectors.size(),
                    documents.size());
        } catch (Exception e) {
            log.error("Failed to load persisted data", e);
        }
    }

    /**
     * Full pipeline: chunk -> embed -> store -> single-pass write to disk.
     */
    public List<Integer> insertDocument(InsertDocumentRequest request) {
        String title = request.getTitle();
        String text  = request.getText();

        List<String> chunks = TextChunker.chunk(text);
        log.info("'{}' split into {} chunk(s)", title, chunks.size());

        String documentId = UUID.randomUUID().toString();
        List<Integer> insertedIds = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);

            double[] raw = ollamaService.embed(chunk);
            if (raw.length == 0) {
                log.warn("Embedding failed at chunk {}/{} for '{}' — aborting batch insertion",
                        i + 1, chunks.size(), title);

                // Save whatever successfully parsed up to this point
                triggerAutoSave();
                return insertedIds;
            }

            List<Double> embedding = new ArrayList<>(raw.length);
            for (double v : raw) {
                embedding.add(v);
            }

            String chunkTitle = chunks.size() > 1
                    ? title + " [" + (i + 1) + "/" + chunks.size() + "]"
                    : title;

            int vectorId = docStore.insert(chunkTitle, "doc", embedding);

            DocItem docItem = DocItem.builder()
                    .id(vectorId)
                    .documentId(documentId)
                    .chunkIndex(i)
                    .title(chunkTitle)
                    .chunkText(chunk)
                    .build();

            metadataStore.put(vectorId, docItem);
            insertedIds.add(vectorId);

            log.debug("Chunk {}/{} staged in-memory as vectorId={}", i + 1, chunks.size(), vectorId);
        }

        // Save exactly ONCE after the entire file array processes cleanly
        triggerAutoSave();
        return insertedIds;
    }

    /**
     * Deletes a chunk by vector ID from both stores and triggers a disk rewrite.
     */
    public boolean delete(int id) {
        boolean vectorRemoved = docStore.delete(id);
        boolean metaRemoved   = metadataStore.remove(id) != null;

        triggerAutoSave();

        return vectorRemoved && metaRemoved;
    }

    public List<DocItem> search(List<Double> embedding, String metric, int k) {
        List<VectorItem> matches = docStore.search(embedding, k, metric);
        List<DocItem> results = new ArrayList<>();

        for (VectorItem item : matches) {
            DocItem doc = metadataStore.get(item.getId());
            if (doc != null) {
                results.add(doc);
            }
        }
        return results;
    }

    public Optional<DocItem> getDocItem(int vectorId) {
        return Optional.ofNullable(metadataStore.get(vectorId));
    }

    public VectorStoreService getDocStore() {
        return docStore;
    }

    public int size() {
        return metadataStore.size();
    }

    // Helper method to consolidate safe disk syncing
    private void triggerAutoSave() {
        try {
            persistenceService.saveAll(docStore.getStore(), metadataStore);
        } catch (Exception e) {
            log.error("Critical: Failed to sync database updates to disk persistence layers!", e);
        }
    }

    private DocumentListResponse toListResponse(DocItem item) {
        String text    = item.getChunkText();
        String preview = text.length() > 120 ? text.substring(0, 120) + "…" : text;
        int wordCount  = text.isBlank() ? 0 : text.trim().split("\\s+").length;

        return DocumentListResponse.builder()
                .id(item.getId())
                .documentId(item.getDocumentId())
                .chunkIndex(item.getChunkIndex())
                .title(item.getTitle())
                .preview(preview)
                .wordCount(wordCount)
                .build();
    }

    public List<DocumentListResponse> listAll() {
        return metadataStore.values().stream()
                .sorted(Comparator.comparingInt(DocItem::getId))
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }
// ─────────────────────────────────────────────────────────────────────
    // Step 13 — Document grouping & bulk delete (additive, Step 13 only)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Groups stored chunks by documentId for the Documents page card view.
     * Reuses metadataStore — no new storage, no duplication.
     * Order: documents appear in the order their first chunk was inserted
     * (vector IDs increase monotonically on insert, so sorting by id
     * before grouping preserves insertion order).
     */
    public List<DocumentSummaryResponse> listGrouped() {
        List<DocItem> orderedItems = metadataStore.values().stream()
                .filter(item -> item.getDocumentId() != null)
                .sorted(Comparator.comparingInt(DocItem::getId))
                .collect(Collectors.toList());

        Map<String, List<DocItem>> byDocId = new LinkedHashMap<>();
        for (DocItem item : orderedItems) {
            byDocId.computeIfAbsent(item.getDocumentId(), k -> new ArrayList<>())
                    .add(item);
        }

        List<DocumentSummaryResponse> summaries = new ArrayList<>();

        for (Map.Entry<String, List<DocItem>> entry : byDocId.entrySet()) {
            String documentId = entry.getKey();
            List<DocItem> chunks = entry.getValue();

            chunks.sort(Comparator.comparingInt(DocItem::getChunkIndex));

            String baseTitle = stripChunkSuffix(chunks.get(0).getTitle());

            List<DocumentListResponse> chunkResponses = chunks.stream()
                    .map(this::toListResponse)
                    .collect(Collectors.toList());

            summaries.add(DocumentSummaryResponse.builder()
                    .documentId(documentId)
                    .title(baseTitle)
                    .totalChunks(chunks.size())
                    .chunks(chunkResponses)
                    .build());
        }

        return summaries;
    }

    /**
     * Removes the " [i/n]" chunk-index suffix appended in insertDocument(),
     * using plain string operations (no regex).
     * Suffix format is always: " [" + number + "/" + number + "]"
     */
    private String stripChunkSuffix(String title) {
        int bracketStart = title.lastIndexOf(" [");
        if (bracketStart == -1 || !title.endsWith("]")) {
            return title;
        }
        return title.substring(0, bracketStart);
    }

    /**
     * Deletes every chunk belonging to a documentId from docStore and
     * metadataStore, then persists once via the existing triggerAutoSave()
     * helper — no changes to persistence logic itself.
     * Returns the number of chunks removed (0 means the documentId did not exist).
     */
    public int deleteDocumentGroup(String documentId) {
        List<Integer> idsToRemove = metadataStore.values().stream()
                .filter(item -> item.getDocumentId().equals(documentId))
                .map(DocItem::getId)
                .collect(Collectors.toList());

        for (int vectorId : idsToRemove) {
            docStore.delete(vectorId);
            metadataStore.remove(vectorId);
        }

        if (!idsToRemove.isEmpty()) {
            triggerAutoSave();
        }

        return idsToRemove.size();
    }
}