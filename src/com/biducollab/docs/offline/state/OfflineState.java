package com.biducollab.docs.offline.state;

import com.biducollab.docs.model.Document;
import com.biducollab.docs.offline.OfflineOperationQueue;
import com.biducollab.docs.operation.EditOperation;

public class OfflineState implements SyncState {

    @Override
    public void handleEdit(
            Document document,
            EditOperation operation,
            OfflineOperationQueue operationQueue) {

        // Apply edit locally
        operation.execute(document);

        // Save operation for later sync
        operationQueue.addOperation(operation);

        System.out.println(
                "Offline: Operation queued -> "
                        + operation.getOperationId()
        );
    }
}
