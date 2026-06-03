// src/main/java/com/vectordb/controller/StatusController.java
package com.vectordb.controller;

import com.vectordb.model.dto.response.StatusResponse;
import com.vectordb.service.OllamaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StatusController {

    private final OllamaService ollamaService;

    @GetMapping("/status")
    public StatusResponse status() {
        return StatusResponse.builder()
                .ollamaAvailable(ollamaService.isAvailable())
                .embedModel(ollamaService.getEmbedModel())
                .genModel(ollamaService.getGenModel())
                .docCount(0)    // wired in Step 5
                .docDims(0)     // wired in Step 5
                .demoCount(0)   // wired in Step 5
                .build();
    }
}