package com.biducollab.docs.presence;

// Represents a user's cursor position in a document
public class CursorPosition {

    private final String userId;
    private final String documentId;

    private int position;

    public CursorPosition(
            String userId,
            String documentId,
            int position) {

        this.userId = userId;
        this.documentId = documentId;
        this.position = position;
    }

    // Update cursor position
    public void updatePosition(int position) {
        this.position = position;
    }

    public String getUserId() {
        return userId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public int getPosition() {
        return position;
    }
}