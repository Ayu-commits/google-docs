package com.biducollab.docs.offline;

import com.biducollab.docs.model.Document;
import com.biducollab.docs.operation.EditOperation;

public interface SyncState {

    // Handle edit based on current sync state
    void handleEdit(
            Document document,
            EditOperation operation,
            OfflineOperationQueue operationQueue
    );
}