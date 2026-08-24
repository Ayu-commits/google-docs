package com.biducollab.docs.offline;

import com.biducollab.docs.offline.state.OfflineState;
import com.biducollab.docs.offline.state.OnlineState;
import com.biducollab.docs.offline.state.SyncingState;

// Manages document connection state
public class ConnectionManager {

    private boolean connected;

    private final DocumentSyncManager syncManager;

    public ConnectionManager(
            DocumentSyncManager syncManager) {

        this.syncManager = syncManager;
        this.connected = true;
    }

    // Mark connection as online
    public void connect() {

        connected = true;

        syncManager.setState(
                new OnlineState()
        );

        System.out.println(
                "Connection restored"
        );
    }

    // Mark connection as offline
    public void disconnect() {

        connected = false;

        syncManager.setState(
                new OfflineState()
        );

        System.out.println(
                "Connection lost"
        );
    }

    // Check connection status
    public boolean isConnected() {
        return connected;
    }

    // Mark sync in progress
    public void startSync() {

        syncManager.setState(
                new SyncingState()
        );
    }
}
