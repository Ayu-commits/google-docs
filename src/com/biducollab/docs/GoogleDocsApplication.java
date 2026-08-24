package com.biducollab.docs;

import com.biducollab.docs.collaboration.CollaborationSession;
import com.biducollab.docs.collaboration.DocumentClient;
import com.biducollab.docs.comment.Comment;
import com.biducollab.docs.comment.CommentService;
import com.biducollab.docs.factory.DocumentFactory;
import com.biducollab.docs.history.DocumentContentSerializer;
import com.biducollab.docs.history.VersionHistory;
import com.biducollab.docs.model.Document;
import com.biducollab.docs.model.PermissionType;
import com.biducollab.docs.operation.OperationLog;
import com.biducollab.docs.permission.AuthorizationService;
import com.biducollab.docs.permission.DocumentPermissionService;
import com.biducollab.docs.permission.DocumentSharingService;
import com.biducollab.docs.presence.CursorService;
import com.biducollab.docs.presence.PresenceService;
import com.biducollab.docs.repository.DocumentRepository;
import com.biducollab.docs.repository.InMemoryDocumentRepository;
import com.biducollab.docs.repository.InMemoryOperationRepository;
import com.biducollab.docs.repository.InMemoryVersionHistoryRepository;
import com.biducollab.docs.repository.OperationRepository;
import com.biducollab.docs.repository.VersionHistoryRepository;
import com.biducollab.docs.service.DocumentService;

// Runs the complete Google Docs LLD demo
public class GoogleDocsApplication {

    public static void main(String[] args) {

        // Create repositories
        DocumentRepository documentRepository =
                new InMemoryDocumentRepository();

        OperationRepository operationRepository =
                new InMemoryOperationRepository();

        VersionHistoryRepository versionHistoryRepository =
                new InMemoryVersionHistoryRepository();


        // Create core services
        DocumentService documentService =
                new DocumentService(
                        documentRepository
                );

        DocumentPermissionService permissionService =
                new DocumentPermissionService();

        DocumentSharingService sharingService =
                new DocumentSharingService(
                        permissionService
                );

        AuthorizationService authorizationService =
                new AuthorizationService();

        OperationLog operationLog =
                new OperationLog(
                        operationRepository
                );


        // Create version history
        VersionHistory versionHistory =
                new VersionHistory(
                        versionHistoryRepository
                );

        DocumentContentSerializer serializer =
                new DocumentContentSerializer();


        // Create presence services
        PresenceService presenceService =
                new PresenceService();

        CursorService cursorService =
                new CursorService();


        // Create document factory
        DocumentFactory documentFactory =
                new DocumentFactory();


        // Create document FIRST
        Document document =
                documentFactory.createDocument(
                        "My Google Document",
                        "user-1"
                );

        // Save document
        documentService.createDocument(document);

        System.out.println(
                "Document created: "
                        + document.getDocumentId()
        );


        // Create collaboration session AFTER document creation
        CollaborationSession collaborationSession =
                new CollaborationSession(
                        document.getDocumentId()
                );


        // Share document with user-2
        sharingService.shareDocument(
                document,
                "user-2",
                PermissionType.EDITOR
        );


        // Users join the document
        presenceService.joinDocument(
                document.getDocumentId(),
                "user-1"
        );

        presenceService.joinDocument(
                document.getDocumentId(),
                "user-2"
        );


        // Create document clients
        DocumentClient user1 =
                new DocumentClient("user-1");

        DocumentClient user2 =
                new DocumentClient("user-2");


        // Add users to receive real-time updates
        collaborationSession.addObserver(user1);
        collaborationSession.addObserver(user2);


        // Update cursor positions
        cursorService.updateCursor(
                document.getDocumentId(),
                "user-1",
                10
        );

        cursorService.updateCursor(
                document.getDocumentId(),
                "user-2",
                25
        );


        // Create comment service
        CommentService commentService =
                new CommentService();


        // Add a comment
        Comment comment =
                new Comment(
                        "comment-1",
                        document.getDocumentId(),
                        "user-1",
                        "element-1",
                        "Please review this section"
                );

        commentService.addComment(comment);


        System.out.println(
                "Google Docs demo initialized successfully"
        );
    }
}