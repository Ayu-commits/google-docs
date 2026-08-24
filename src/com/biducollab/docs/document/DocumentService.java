package com.biducollab.docs.document;

import com.biducollab.docs.model.Document;
import com.biducollab.docs.repository.DocumentRepository;

import java.util.List;

/**
 * Manages the basic lifecycle of a {@link Document}:
 * creation, retrieval, listing, and deletion.
 * All persistence is delegated to a {@link DocumentRepository}.
 */
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
