package com.biducollab.docs.operation;

import com.biducollab.docs.operation.conflict.ConflictResolutionStrategy;
import com.biducollab.docs.model.Document;

import java.time.Instant;
import java.util.List;

/**
 * Coordinates the full server-side edit pipeline:
 *   1. Stamp a server timestamp for deterministic OT ordering.
 *   2. Collect concurrent operations since the client's baseVersion.
 *   3. Resolve conflicts via the configured ConflictResolutionStrategy.
 *   4. Execute the transformed operation on the document.
 *   5. Persist the operation to the log.
 */
public class OperationProcessor {

    private final OperationLog operationLog;
    private final ConflictResolutionStrategy conflictResolutionStrategy;

    public OperationProcessor(
            OperationLog operationLog,
            ConflictResolutionStrategy conflictResolutionStrategy) {

        this.operationLog = operationLog;
        this.conflictResolutionStrategy = conflictResolutionStrategy;
    }

    // Process an incoming operation through the OT pipeline.
    public EditOperation process(
            Document document,
            EditOperation incomingOperation) {

        // Assign server timestamp for deterministic tie-breaking in OT.
        incomingOperation.setServerTimestamp(Instant.now());

        // Collect operations applied after the client's base version.
        List<EditOperation> concurrentOperations =
                operationLog.getOperationsAfterVersion(
                        incomingOperation.getDocumentId(),
                        incomingOperation.getBaseVersion()
                );

        // Transform the incoming operation against concurrent operations.
        EditOperation transformedOperation =
                conflictResolutionStrategy.resolve(
                        incomingOperation,
                        concurrentOperations
                );

        // Apply the transformed operation to the document.
        transformedOperation.execute(document);

        // Persist the applied operation for future transforms.
        operationLog.addOperation(transformedOperation);

        return transformedOperation;
    }
}