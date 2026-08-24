package com.biducollab.docs.collaboration;

import java.util.HashMap;
import java.util.Map;

// Manages collaboration sessions for multiple documents
public class CollaborationSessionManager {

    private final Map<String, CollaborationSession>
            sessionsByDocument = new HashMap<>();

    // Get or create collaboration session
    public CollaborationSession getOrCreateSession(
            String documentId) {

        return sessionsByDocument.computeIfAbsent(
                documentId,
                key -> new CollaborationSession(key)
        );
    }

    // Get existing session
    public CollaborationSession getSession(
            String documentId) {

        return sessionsByDocument.get(documentId);
    }

    // Remove session when no longer needed
    public void closeSession(
            String documentId) {

        sessionsByDocument.remove(documentId);
    }
}