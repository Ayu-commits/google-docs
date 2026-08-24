package com.biducollab.docs.factory;

import com.biducollab.docs.model.Document;

import java.util.UUID;

// Creates documents with default values
public class DocumentFactory {

    // Create a new document
    public Document createDocument(
            String title,
            String ownerId) {

        String documentId =
                UUID.randomUUID().toString();

        return new Document(
                documentId,
                title,
                ownerId
        );
    }
}