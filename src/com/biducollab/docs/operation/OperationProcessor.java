package com.biducollab.docs.operation;

import com.biducollab.docs.conflict.ConflictResolutionStrategy;
import com.biducollab.docs.model.Document;

/*
Jab multiple users same document ko simultaneously edit kar rahe hote hain, tab incoming operation
ko direct apply nahi kar sakte. Pehle check karna padega ki user kis old version par edit kar raha tha,
uske baad wali operations ke against Operational
Transformation (OT) apply karke operation ko latest document state ke according transform karna hoga.
 */
import java.util.List;

public class OperationProcessor {

    private final OperationLog operationLog;
    private final ConflictResolutionStrategy conflictResolutionStrategy;

    public OperationProcessor(
            OperationLog operationLog,
            ConflictResolutionStrategy conflictResolutionStrategy) {

        this.operationLog = operationLog;
        this.conflictResolutionStrategy = conflictResolutionStrategy;
    }

    // Process incoming operation safely
    public EditOperation process(
            Document document,
            EditOperation incomingOperation) {

        // Get operations applied after user's version
        List<EditOperation> concurrentOperations =
                operationLog.getOperationsAfterVersion(
                        incomingOperation.getDocumentId(),
                        incomingOperation.getBaseVersion()
                );

        // Transform incoming operation
        EditOperation transformedOperation =
                conflictResolutionStrategy.resolve(
                        incomingOperation,
                        concurrentOperations
                );

        // Apply transformed operation
        transformedOperation.execute(document);

        // Save applied operation
        operationLog.addOperation(
                transformedOperation
        );

        return transformedOperation;
    }
}
/*
OperationProcessor
        |
        ├── OperationLog
        │       → Concurrent operations find karta hai
        │
        ├── ConflictResolutionStrategy
        │       → Conflict resolve karta hai
        │
        └── Document
                → Final transformed operation apply karta hai
 */