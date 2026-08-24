package com.biducollab.docs.event;

// Represents an event happening in a document
public class DocumentEvent {

    private final String documentId;
    private final String userId;
    private final String eventType;

    public DocumentEvent(
            String documentId,
            String userId,
            String eventType) {

        this.documentId = documentId;
        this.userId = userId;
        this.eventType = eventType;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getUserId() {
        return userId;
    }

    public String getEventType() {
        return eventType;
    }
}