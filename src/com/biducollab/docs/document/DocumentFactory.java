package com.biducollab.docs.document;

import com.biducollab.docs.model.Document;

import java.util.UUID;

/** Creates {@link Document} instances with generated IDs and default values. */
public class DocumentFactory {

    /** Create a new document with a random ID. */
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
