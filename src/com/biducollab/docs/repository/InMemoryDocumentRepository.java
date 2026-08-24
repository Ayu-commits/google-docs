package com.biducollab.docs.repository;

import com.biducollab.docs.model.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Stores documents in memory using HashMap
public class InMemoryDocumentRepository
        implements DocumentRepository {

    private final Map<String, Document> documents =
            new HashMap<>();

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