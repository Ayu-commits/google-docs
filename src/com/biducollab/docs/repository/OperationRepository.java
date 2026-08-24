package com.biducollab.docs.repository;

import com.biducollab.docs.operation.EditOperation;

import java.util.List;

// Defines how document operations are stored and retrieved
public interface OperationRepository {

    // Save an applied operation
    void save(EditOperation operation);

    // Get all operations for a document
    List<EditOperation> findByDocumentId(
            String documentId
    );

    // Get operations after a specific version
    List<EditOperation> findAfterVersion(
            String documentId,
            int baseVersion
    );

    // Get total operations for a document
    int countByDocumentId(String documentId);
}