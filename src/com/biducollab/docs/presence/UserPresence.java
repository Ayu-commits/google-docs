package com.biducollab.docs.presence;

import java.time.LocalDateTime;

// Represents a user's presence in a document
public class UserPresence {

    private final String userId;

    private final String documentId;

    private LocalDateTime lastActiveAt;

    public UserPresence(
            String userId,
            String documentId) {

        this.userId = userId;
        this.documentId = documentId;
        this.lastActiveAt = LocalDateTime.now();
    }

    // Update user's activity time
    public void updateActivity() {

        lastActiveAt = LocalDateTime.now();
    }

    public String getUserId() {
        return userId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }
}