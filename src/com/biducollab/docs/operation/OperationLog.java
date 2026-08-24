package com.biducollab.docs.operation;

import com.biducollab.docs.repository.OperationRepository;

import java.util.List;

// Manages applied operations using repository
public class OperationLog {

    private final OperationRepository operationRepository;

    public OperationLog(
            OperationRepository operationRepository) {

        this.operationRepository = operationRepository;
    }

    // Save successfully applied operation
    public void addOperation(
            EditOperation operation) {

        operationRepository.save(operation);
    }

    // Get operations after a user's base version
    public List<EditOperation> getOperationsAfterVersion(
            String documentId,
            int baseVersion) {

        return operationRepository.findAfterVersion(
                documentId,
                baseVersion
        );
    }

    // Get all operations for a document
    public List<EditOperation> getAllOperations(
            String documentId) {

        return operationRepository.findByDocumentId(
                documentId
        );
    }

    // Get total operations for a document
    public int size(String documentId) {

        return operationRepository.countByDocumentId(
                documentId
        );
    }
}
/*
Incoming Operation
       ↓
Check baseVersion
       ↓
OperationLog se concurrent operations nikalo
       ↓
OT Strategy se transform karo
       ↓
Execute transformed operation
       ↓
OperationLog mein save karo
 */