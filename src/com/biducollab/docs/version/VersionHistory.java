package com.biducollab.docs.version;

import com.biducollab.docs.repository.VersionHistoryRepository;

import java.util.List;

/**
 * Thin façade over {@link VersionHistoryRepository} that exposes
 * snapshot save/lookup by document ID and version number.
 */
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
Flow

User edits document
        |
        v
Document version increments to N
        |
        v
DocumentCollaborationManager creates a DocumentSnapshot
        |
        v
VersionHistory.addSnapshot()
        |
        v
VersionHistoryRepository stores snapshot keyed by (documentId, version)
 */
