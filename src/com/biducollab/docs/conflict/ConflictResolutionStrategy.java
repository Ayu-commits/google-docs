package com.biducollab.docs.conflict;

import com.biducollab.docs.operation.EditOperation;

import java.util.List;

public interface ConflictResolutionStrategy {

    // Resolve an incoming operation against concurrent operations
    EditOperation resolve(
            EditOperation incomingOperation,
            List<EditOperation> concurrentOperations
    );
}