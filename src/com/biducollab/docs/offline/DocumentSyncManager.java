package com.biducollab.docs.offline;

import com.biducollab.docs.model.Document;
import com.biducollab.docs.operation.EditOperation;

public class DocumentSyncManager {

    // Current state of document synchronization
    private SyncState currentState;

    private final OfflineOperationQueue operationQueue;

    public DocumentSyncManager(
            SyncState initialState,
            OfflineOperationQueue operationQueue) {

        this.currentState = initialState;
        this.operationQueue = operationQueue;
    }

    // Change current sync state
    public void setState(SyncState state) {
        this.currentState = state;
    }

    // Handle edit based on current state
    public void handleEdit(
            Document document,
            EditOperation operation) {

        currentState.handleEdit(
                document,
                operation,
                operationQueue
        );
    }

    public OfflineOperationQueue getOperationQueue() {
        return operationQueue;
    }
}
/*
Ab complete State Pattern
                    SyncState
                        ▲
                        |
          +-------------+-------------+
          |             |             |
          |             |             |
     OnlineState   OfflineState   SyncingState
          ▲             ▲             ▲
          |             |             |
          +-------------+-------------+
                        |
                        |
              DocumentSyncManager
                        |
                        v
             OfflineOperationQueue

 */