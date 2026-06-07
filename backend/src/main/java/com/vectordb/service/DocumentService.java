// src/main/java/com/vectordb/service/DocumentService.java
package com.vectordb.service;

import com.vectordb.core.TextChunker;
import com.vectordb.model.DocItem;
import com.vectordb.model.VectorItem;
import com.vectordb.model.dto.request.InsertDocumentRequest;
import com.vectordb.model.dto.response.DocumentListResponse;
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

    // Parallel metadata store: vectorId -> DocItem
    // VectorStoreService owns the embedding; this map owns all other metadata.
    private final Map<Integer, DocItem> metadataStore = new ConcurrentHashMap<>();

    public DocumentService(
            OllamaService ollamaService,
            @Qualifier("docVectorStore") VectorStoreService docStore) {

        this.ollamaService = ollamaService;
        this.docStore = docStore;
    }

    /**
     * Full pipeline: chunk -> embed -> store.
     *
     * Returns vector IDs of all successfully inserted chunks.
     * Returns empty list if Ollama is unavailable on first chunk.
     * Returns partial list if Ollama fails mid-document.
     */
    public List<Integer> insertDocument(InsertDocumentRequest request) {
        String title = request.getTitle();
        String text  = request.getText();

        // Step 1: Chunk — static call, no injection needed
        List<String> chunks = TextChunker.chunk(text);
        log.info("'{}' split into {} chunk(s)", title, chunks.size());

        // Step 2: One UUID groups all chunks from this logical document
        String documentId = UUID.randomUUID().toString();

        List<Integer> insertedIds = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);

            // Step 3: Embed via Ollama
            double[] raw = ollamaService.embed(chunk);

            if (raw.length == 0) {
                log.warn("Embedding failed at chunk {}/{} for '{}' — aborting",
                        i + 1, chunks.size(), title);
                // Return what was stored so far; controller decides the response
                return insertedIds;
            }

            // Step 4: double[] -> List<Double> for VectorStoreService
            List<Double> embedding = new ArrayList<>(raw.length);
            for (double v : raw) {
                embedding.add(v);
            }

            // Step 5: Build display title
            String chunkTitle = chunks.size() > 1
                    ? title + " [" + (i + 1) + "/" + chunks.size() + "]"
                    : title;

            // Step 6: Insert vector — returns assigned ID
            int vectorId = docStore.insert(chunkTitle, "doc", embedding);

            // Step 7: Save metadata linked by vectorId
            DocItem docItem = DocItem.builder()
                    .id(vectorId)
                    .documentId(documentId)
                    .chunkIndex(i)
                    .title(chunkTitle)
                    .chunkText(chunk)
                    .build();

            metadataStore.put(vectorId, docItem);
            insertedIds.add(vectorId);

            log.debug("Chunk {}/{} stored as vectorId={}", i + 1, chunks.size(), vectorId);
        }

        return insertedIds;
    }

    /**
     * Returns all stored chunks sorted by vector ID (insertion order).
     */
    public List<DocumentListResponse> listAll() {
        return metadataStore.values().stream()
                .sorted(Comparator.comparingInt(DocItem::getId))
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a chunk by vector ID from both the vector store and metadata store.
     */
    public boolean delete(int id) {
        boolean vectorRemoved = docStore.delete(id);
        boolean metaRemoved   = metadataStore.remove(id) != null;
        return vectorRemoved && metaRemoved;
    }
    public List<DocItem> search(
        List<Double> embedding,
        String metric,
        int k) {

    List<VectorItem> matches = docStore.search(
            embedding,
            k,
            metric
    );

    List<DocItem> results = new ArrayList<>();

    for (VectorItem item : matches) {
        DocItem doc = metadataStore.get(item.getId());

        if (doc != null) {
            results.add(doc);
        }
    }

    return results;
}
    /**
     * Retrieves DocItem metadata by vector ID.
     * RagService will call this in Step 7 after a similarity search.
     */
    public Optional<DocItem> getDocItem(int vectorId) {
        return Optional.ofNullable(metadataStore.get(vectorId));
    }

    /**
     * Returns the docStore reference for RagService to run similarity search.
     * Step 7 will use: docStore.search(queryEmbedding, k, "cosine")
     */
    public VectorStoreService getDocStore() {
        return docStore;
    }

    /**
     * Total number of stored chunks.
     */
    public int size() {
        return metadataStore.size();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private DocumentListResponse toListResponse(DocItem item) {
        String text    = item.getChunkText();
        String preview = text.length() > 120
                ? text.substring(0, 120) + "…"
                : text;
        int wordCount  = text.isBlank()
                ? 0
                : text.trim().split("\\s+").length;

        return DocumentListResponse.builder()
                .id(item.getId())
                .documentId(item.getDocumentId())
                .chunkIndex(item.getChunkIndex())
                .title(item.getTitle())
                .preview(preview)
                .wordCount(wordCount)
                .build();
    }
}