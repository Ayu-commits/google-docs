package com.biducollab.docs.repository;

import com.biducollab.docs.operation.EditOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// Stores document edit operations in memory; thread-safe via ConcurrentHashMap.
public class InMemoryOperationRepository
        implements OperationRepository {

    private final Map<String, List<EditOperation>>
            operationsByDocument = new ConcurrentHashMap<>();

    @Override
    public void save(EditOperation operation) {

        operationsByDocument
                .computeIfAbsent(
                        operation.getDocumentId(),
                        key -> new CopyOnWriteArrayList<>()
                )
                .add(operation);
    }

    @Override
    public List<EditOperation> findByDocumentId(
            String documentId) {

        return new ArrayList<>(
                operationsByDocument.getOrDefault(
                        documentId,
                        new ArrayList<>()
                )
        );
    }

    @Override
    public List<EditOperation> findAfterVersion(
            String documentId,
            int baseVersion) {

        List<EditOperation> result =
                new ArrayList<>();

        List<EditOperation> operations =
                operationsByDocument.getOrDefault(
                        documentId,
                        new ArrayList<>()
                );

        for (EditOperation operation : operations) {

            // Get operations after user's base version
            if (operation.getBaseVersion() >= baseVersion) {
                result.add(operation);
            }
        }

        return result;
    }

    @Override
    public int countByDocumentId(
            String documentId) {

        return operationsByDocument
                .getOrDefault(
                        documentId,
                        new ArrayList<>()
                )
                .size();
    }
}