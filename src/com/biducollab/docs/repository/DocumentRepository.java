package com.biducollab.docs.repository;

import com.biducollab.docs.model.Document;

import java.util.List;

// Defines how documents are stored and retrieved
public interface DocumentRepository {

    // Save a document
    void save(Document document);

    // Find document by id
    Document findById(String documentId);

    // Get all documents
    List<Document> findAll();

    // Delete document by id
    void deleteById(String documentId);

    // Check if document exists
    boolean existsById(String documentId);
}