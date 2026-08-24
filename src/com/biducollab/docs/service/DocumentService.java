package com.biducollab.docs.service;

import com.biducollab.docs.model.Document;

import java.util.List;
import com.biducollab.docs.repository.DocumentRepository;


/*
Ye class document ka basic lifecycle manage karegi:
Naya document create karna
Document fetch karna
Document delete karna
User ke documents fetch karna
 */

// Manages document creation, retrieval and deletion
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(
            DocumentRepository documentRepository) {

        this.documentRepository = documentRepository;
    }

    // Create a new document
    public void createDocument(Document document) {

        if (documentRepository.existsById(
                document.getDocumentId())) {

            throw new IllegalArgumentException(
                    "Document already exists"
            );
        }

        documentRepository.save(document);
    }

    // Get document by id
    public Document getDocument(
            String documentId) {

        Document document =
                documentRepository.findById(documentId);

        if (document == null) {
            throw new IllegalArgumentException(
                    "Document not found"
            );
        }

        return document;
    }

    // Get all documents
    public List<Document> getAllDocuments() {

        return documentRepository.findAll();
    }

    // Delete document
    public void deleteDocument(
            String documentId) {

        if (!documentRepository.existsById(documentId)) {

            throw new IllegalArgumentException(
                    "Document not found"
            );
        }

        documentRepository.deleteById(documentId);
    }
}