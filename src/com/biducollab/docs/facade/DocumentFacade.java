package com.biducollab.docs.facade;

import com.biducollab.docs.collaboration.DocumentCollaborationManager;
import com.biducollab.docs.comment.Comment;
import com.biducollab.docs.comment.CommentService;
import com.biducollab.docs.model.Document;
import com.biducollab.docs.model.PermissionType;
import com.biducollab.docs.operation.EditOperation;

import com.biducollab.docs.permission.DocumentSharingService;

import com.biducollab.docs.presence.PresenceService;
import com.biducollab.docs.service.DocumentService;

// Provides a simple API for document operations
public class DocumentFacade {

    private final DocumentService documentService;

    private final DocumentSharingService sharingService;

    private final DocumentCollaborationManager collaborationManager;

    private final CommentService commentService;

    private final PresenceService presenceService;

    public DocumentFacade(
            DocumentService documentService,
            DocumentSharingService sharingService,
            DocumentCollaborationManager collaborationManager,
            CommentService commentService,
            PresenceService presenceService) {

        this.documentService = documentService;
        this.sharingService = sharingService;
        this.collaborationManager = collaborationManager;
        this.commentService = commentService;
        this.presenceService = presenceService;
    }

    // Create a document
    public void createDocument(Document document) {

        documentService.createDocument(document);
    }

    // User joins a document
    public void joinDocument(
            String documentId,
            String userId) {

        presenceService.joinDocument(
                documentId,
                userId
        );
    }

    // User edits a document
    public void editDocument(
            Document document,
            EditOperation operation) {

        collaborationManager.handleEdit(
                document,
                operation
        );
    }

    // Share document with another user
    public void shareDocument(
            Document document,
            String userId,
            PermissionType permissionType) {

        sharingService.shareDocument(
                document,
                userId,
                permissionType
        );
    }

    // Add a comment
    public void addComment(Comment comment) {

        commentService.addComment(comment);
    }

    // User leaves a document
    public void leaveDocument(
            String documentId,
            String userId) {

        presenceService.leaveDocument(
                documentId,
                userId
        );
    }
}