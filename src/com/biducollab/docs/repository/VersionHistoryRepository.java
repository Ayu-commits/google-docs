package com.biducollab.docs.repository;

import com.biducollab.docs.history.DocumentSnapshot;

import java.util.List;

// Defines how document version history is stored
public interface VersionHistoryRepository {

    // Save a document snapshot
    void save(DocumentSnapshot snapshot);

    // Get all versions of a document
    List<DocumentSnapshot> findByDocumentId(
            String documentId
    );

    // Get a specific document version
    DocumentSnapshot findByDocumentIdAndVersion(
            String documentId,
            int version
    );

    // Get latest document version
    DocumentSnapshot findLatest(
            String documentId
    );
}