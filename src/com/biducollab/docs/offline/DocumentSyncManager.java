package com.biducollab.docs.offline;

import com.biducollab.docs.model.Document;
import com.biducollab.docs.offline.state.SyncState;
import com.biducollab.docs.operation.EditOperation;

/**
 * Context class for the State pattern governing offline/online synchronisation.
 *
 * <p>Delegates {@code handleEdit()} to the active {@link SyncState}, which is
 * one of {@code OnlineState}, {@code OfflineState}, or {@code SyncingState}.
 * State transitions are driven externally (e.g. network connectivity events).
 */
public class DocumentSyncManager {

    /** The active sync state; mutated by {@link #setState}. */
    private SyncState currentState;

    private final OfflineOperationQueue operationQueue;

    public DocumentSyncManager(
            SyncState initialState,
            OfflineOperationQueue operationQueue) {

        this.currentState = initialState;
        this.operationQueue = operationQueue;
    }

    /** Transition to a new sync state. */
    public void setState(SyncState state) {
        this.currentState = state;
    }

    /**
     * Route an edit through the current sync state.
     * Online: executes immediately; Offline: queues for later replay.
     */
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
State Pattern diagram

               SyncState  (interface)
                   ▲
                   |
     +-------------+-------------+
     |             |             |
 OnlineState  OfflineState  SyncingState
                   |
                   v
       OfflineOperationQueue  (buffers ops)
                   |
                   v
            SyncService.sync()  (replays queue on reconnect)
 */
