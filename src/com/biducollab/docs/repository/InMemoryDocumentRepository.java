package com.biducollab.docs.repository;

import com.biducollab.docs.model.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Stores documents in memory; ConcurrentHashMap ensures thread-safe access.
public class InMemoryDocumentRepository
        implements DocumentRepository {

    private final Map<String, Document> documents =
            new ConcurrentHashMap<>();

    @Override
    public void save(Document document) {

        documents.put(
                document.getDocumentId(),
                document
        );
    }

    @Override
    public Document findById(String documentId) {

        return documents.get(documentId);
    }

    @Override
    public List<Document> findAll() {

        return new ArrayList<>(
                documents.values()
        );
    }

    @Override
    public void deleteById(String documentId) {

        documents.remove(documentId);
    }

    @Override
    public boolean existsById(String documentId) {

        return documents.containsKey(documentId);
    }
}