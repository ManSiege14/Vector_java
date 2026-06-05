package com.vectordb.model.dto.request;

import lombok.Data;

@Data
public class InsertDocumentRequest {

    private String title; // Document title supplied by client
    private String text;  // Full document body — will be chunked internally
}