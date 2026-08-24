package com.biducollab.docs.version;

import java.time.LocalDateTime;

public class DocumentSnapshot {

    private final String documentId;
    private final int version;
    private final String content;
    private final String modifiedBy;
    private final LocalDateTime modifiedAt;

    public DocumentSnapshot(
            String documentId,
            int version,
            String content,
            String modifiedBy) {

        this.documentId = documentId;
        this.version = version;
        this.content = content;
        this.modifiedBy = modifiedBy;

        // Capture snapshot creation time
        this.modifiedAt = LocalDateTime.now();
    }

    public String getDocumentId() {
        return documentId;
    }

    public int getVersion() {
        return version;
    }

    public String getContent() {
        return content;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }
}
