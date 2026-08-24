package com.biducollab.docs.history;

import com.biducollab.docs.repository.VersionHistoryRepository;

import java.util.List;

// Manages document version history
public class VersionHistory {

    private final VersionHistoryRepository versionHistoryRepository;

    public VersionHistory(
            VersionHistoryRepository versionHistoryRepository) {

        this.versionHistoryRepository =
                versionHistoryRepository;
    }

    // Save a new document snapshot
    public void addSnapshot(
            DocumentSnapshot snapshot) {

        versionHistoryRepository.save(snapshot);
    }

    // Get all versions of a document
    public List<DocumentSnapshot> getHistory(
            String documentId) {

        return versionHistoryRepository
                .findByDocumentId(documentId);
    }

    // Get a specific version
    public DocumentSnapshot getVersion(
            String documentId,
            int version) {

        return versionHistoryRepository
                .findByDocumentIdAndVersion(
                        documentId,
                        version
                );
    }

    // Get latest version
    public DocumentSnapshot getLatestVersion(
            String documentId) {

        return versionHistoryRepository
                .findLatest(documentId);
    }
}
/*
Iska flow
User edits document
        |
        v
Document Version = 2
        |
        v
Create DocumentSnapshot
        |
        v
VersionHistory
        |
        ├── Version 1
        ├── Version 2
        ├── Version 3
        └── Version 4

 */