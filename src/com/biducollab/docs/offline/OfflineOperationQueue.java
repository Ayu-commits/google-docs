package com.biducollab.docs.offline;

import com.biducollab.docs.operation.EditOperation;

import java.util.ArrayList;
import java.util.List;

public class OfflineOperationQueue {

    // Stores operations created while offline
    private final List<EditOperation> pendingOperations =
            new ArrayList<>();

    // Add operation to offline queue
    public void addOperation(EditOperation operation) {
        pendingOperations.add(operation);
    }

    // Get all pending operations
    public List<EditOperation> getPendingOperations() {
        return new ArrayList<>(pendingOperations);
    }

    // Check if pending operations exist
    public boolean hasPendingOperations() {
        return !pendingOperations.isEmpty();
    }

    // Clear operations after successful sync
    public void clear() {
        pendingOperations.clear();
    }
}