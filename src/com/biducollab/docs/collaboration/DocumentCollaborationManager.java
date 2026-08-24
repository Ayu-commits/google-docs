package com.biducollab.docs.collaboration;

import com.biducollab.docs.history.DocumentContentSerializer;
import com.biducollab.docs.history.DocumentSnapshot;
import com.biducollab.docs.history.VersionHistory;
import com.biducollab.docs.model.Document;
import com.biducollab.docs.operation.EditOperation;
import com.biducollab.docs.operation.OperationProcessor;
import com.biducollab.docs.permission.AuthorizationService;

//Ye hamare Google Docs LLD ka main coordinator hoga. Jab user edit karega,
// ye class poora flow manage karegi:
public class DocumentCollaborationManager {

    private final AuthorizationService authorizationService;

    private final OperationProcessor operationProcessor;

    private final VersionHistory versionHistory;

    private final CollaborationSession collaborationSession;

    private final DocumentContentSerializer documentContentSerializer;

    public DocumentCollaborationManager(
            AuthorizationService authorizationService,
            OperationProcessor operationProcessor,
            VersionHistory versionHistory,
            CollaborationSession collaborationSession,
            DocumentContentSerializer documentContentSerializer) {

        this.authorizationService = authorizationService;
        this.operationProcessor = operationProcessor;
        this.versionHistory = versionHistory;
        this.collaborationSession = collaborationSession;
        this.documentContentSerializer =
                documentContentSerializer;
    }

    // Handle a user's document edit
    public void handleEdit(
            Document document,
            EditOperation operation) {

        // Check edit permission
        if (!authorizationService.canEdit(
                document,
                operation.getUserId())) {

            throw new SecurityException(
                    "User does not have edit permission"
            );
        }

        // Process operation using OT
        EditOperation processedOperation =
                operationProcessor.process(
                        document,
                        operation
                );

        // Save current document version
        saveSnapshot(
                document,
                operation.getUserId()
        );

        // Notify other connected users
        collaborationSession.broadcast(
                processedOperation
        );
    }

    // Save document state for version history
    private void saveSnapshot(
            Document document,
            String userId) {

        // Convert document into snapshot content
        String content =
                documentContentSerializer.serialize(
                        document
                );

        DocumentSnapshot snapshot =
                new DocumentSnapshot(
                        document.getDocumentId(),
                        document.getCurrentVersion(),
                        content,
                        userId
                );

        versionHistory.addSnapshot(snapshot);
    }
}

/*
Complete flow

Ab maan lo User A document edit karta hai:

User A
   |
   | InsertTextOperation
   v
DocumentCollaborationManager
   |
   +--> AuthorizationService
   |       |
   |       +--> Can edit?
   |
   +--> OperationProcessor
   |       |
   |       +--> OperationLog
   |       |
   |       +--> OT Strategy
   |       |
   |       +--> Apply operation
   |
   +--> VersionHistory
   |       |
   |       +--> Save Snapshot
   |
   +--> CollaborationSession
           |
           +--> User B notified
           |
           +--> User C notified
Is class mein kaun-kaun connected hai?
DocumentCollaborationManager
        |
        ├── AuthorizationService
        │       → Permission check
        │
        ├── OperationProcessor
        │       → OT + operation processing
        │
        ├── VersionHistory
        │       → Document versions
        │
        └── CollaborationSession
                → Real-time broadcast
 */