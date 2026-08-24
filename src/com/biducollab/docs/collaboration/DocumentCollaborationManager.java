package com.biducollab.docs.collaboration;

import com.biducollab.docs.document.DocumentContentSerializer;
import com.biducollab.docs.operation.CommandHistory;
import com.biducollab.docs.version.DocumentSnapshot;
import com.biducollab.docs.version.VersionHistory;
import com.biducollab.docs.model.Document;
import com.biducollab.docs.operation.EditOperation;
import com.biducollab.docs.operation.OperationProcessor;
import com.biducollab.docs.permission.AuthorizationService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Central coordinator for the real-time edit pipeline.
 *
 * <p>When a user submits an edit, this class:
 * <ol>
 *   <li>Verifies the user holds EDITOR permission.</li>
 *   <li>Stamps a server timestamp and runs Operational Transformation
 *       via {@link OperationProcessor}.</li>
 *   <li>Records the transformed operation in the user's personal
 *       undo/redo stack.</li>
 *   <li>Saves a document snapshot to version history.</li>
 *   <li>Broadcasts the transformed operation to all other connected clients.</li>
 * </ol>
 *
 * <p>Each user gets an independent {@link CommandHistory} so that
 * undo/redo is scoped per user, not globally shared.
 *
 * <pre>
 * User A edit
 *   → AuthorizationService.canEdit()
 *   → OperationProcessor.process()   (OT + execute + persist)
 *   → CommandHistory.record()        (per-user undo stack)
 *   → saveSnapshot()                 (version history)
 *   → CollaborationSession.broadcast() (real-time notify)
 * </pre>
 */
public class DocumentCollaborationManager {

    private final AuthorizationService authorizationService;
    private final OperationProcessor operationProcessor;
    private final VersionHistory versionHistory;
    private final CollaborationSession collaborationSession;
    private final DocumentContentSerializer documentContentSerializer;

    /** Per-user undo/redo stacks. Initialised lazily on first edit. */
    private final Map<String, CommandHistory> userCommandHistories =
            new ConcurrentHashMap<>();

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
        this.documentContentSerializer = documentContentSerializer;
    }

    /**
     * Process an incoming edit through the full OT pipeline.
     *
     * @param document  the live document being edited
     * @param operation the raw operation as received from the client
     * @throws SecurityException if the user lacks EDITOR permission
     */
    public void handleEdit(
            Document document,
            EditOperation operation) {

        if (!authorizationService.canEdit(
                document,
                operation.getUserId())) {

            throw new SecurityException(
                    "User does not have edit permission: "
                            + operation.getUserId()
            );
        }

        // OT pipeline: timestamp → transform → execute → persist log.
        EditOperation processedOperation =
                operationProcessor.process(document, operation);

        // Record in the submitting user's undo stack (operation already executed).
        commandHistoryFor(operation.getUserId())
                .record(processedOperation);

        // Snapshot for version history.
        saveSnapshot(document, operation.getUserId());

        // Notify all other connected clients.
        collaborationSession.broadcast(processedOperation);
    }

    /**
     * Undo the most recent edit made by the given user.
     *
     * @param userId   the user requesting undo
     * @param document the document to apply the undo on
     */
    public void undo(String userId, Document document) {
        commandHistoryFor(userId).undo(document);
    }

    /**
     * Redo the most recently undone edit for the given user.
     *
     * @param userId   the user requesting redo
     * @param document the document to apply the redo on
     */
    public void redo(String userId, Document document) {
        commandHistoryFor(userId).redo(document);
    }

    // ------------------------------------------------------------------ //
    //  Private helpers                                                      //
    // ------------------------------------------------------------------ //

    /** Returns (or creates) the CommandHistory for a given user. */
    private CommandHistory commandHistoryFor(String userId) {
        return userCommandHistories.computeIfAbsent(
                userId,
                id -> new CommandHistory()
        );
    }

    /** Serialize and snapshot the current document state. */
    private void saveSnapshot(Document document, String userId) {

        String content = documentContentSerializer.serialize(document);

        DocumentSnapshot snapshot = new DocumentSnapshot(
                document.getDocumentId(),
                document.getCurrentVersion(),
                content,
                userId
        );

        versionHistory.addSnapshot(snapshot);
    }
}
