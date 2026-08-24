package com.biducollab.docs.offline;

import com.biducollab.docs.model.Document;
import com.biducollab.docs.operation.EditOperation;
import com.biducollab.docs.operation.OperationProcessor;

import java.util.List;

// Syncs pending offline operations when connection returns
public class SyncService {

    private final OperationProcessor operationProcessor;

    public SyncService(
            OperationProcessor operationProcessor) {

        this.operationProcessor = operationProcessor;
    }

    // Sync all pending offline operations
    public void sync(
            Document document,
            OfflineOperationQueue operationQueue) {

        List<EditOperation> operations =
                operationQueue.getPendingOperations();

        for (EditOperation operation : operations) {

            operationProcessor.process(
                    document,
                    operation
            );
        }

        // Clear queue after successful sync
        operationQueue.clear();

        System.out.println(
                "Offline operations synced successfully"
        );
    }
}