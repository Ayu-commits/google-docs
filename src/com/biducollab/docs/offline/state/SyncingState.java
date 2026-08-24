package com.biducollab.docs.offline.state;

import com.biducollab.docs.model.Document;
import com.biducollab.docs.offline.OfflineOperationQueue;
import com.biducollab.docs.operation.EditOperation;

public class SyncingState implements SyncState {

    @Override
    public void handleEdit(
            Document document,
            EditOperation operation,
            OfflineOperationQueue operationQueue) {

        // Apply new edit locally
        operation.execute(document);

        // Queue edit until sync completes
        operationQueue.addOperation(operation);

        System.out.println(
                "Syncing: Operation queued -> "
                        + operation.getOperationId()
        );
    }
}
/*
                 SyncState
                     |
        +------------+------------+
        |            |            |
        v            v            v
     Online       Offline       Syncing
        |            |            |
 Execute now    Execute +      Execute +
                Queue          Queue
 */
