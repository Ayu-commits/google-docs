package com.biducollab.docs.operation;

import com.biducollab.docs.model.Document;

import java.time.Instant;

/**
 * Represents a single user edit as a command object.
 * Supports execute/undo for undo-redo, and carries baseVersion
 * and serverTimestamp for Operational Transformation ordering.
 */
public interface EditOperation {

    String getOperationId();

    String getDocumentId();

    String getUserId();

    int getBaseVersion();

    /**
     * Server-assigned timestamp used as a deterministic tie-breaker
     * when two operations arrive with the same baseVersion.
     */
    Instant getServerTimestamp();

    void setServerTimestamp(Instant timestamp);

    void execute(Document document);

    void undo(Document document);
}