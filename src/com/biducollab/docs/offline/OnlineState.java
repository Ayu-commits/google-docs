package com.biducollab.docs.offline;

import com.biducollab.docs.model.Document;
import com.biducollab.docs.operation.EditOperation;

public class OnlineState implements SyncState {

    @Override
    public void handleEdit(
            Document document,
            EditOperation operation,
            OfflineOperationQueue operationQueue) {

        // Apply edit immediately
        operation.execute(document);

        // In real system, send operation to server here
        System.out.println(
                "Online: Operation executed -> "
                        + operation.getOperationId()
        );
    }
}