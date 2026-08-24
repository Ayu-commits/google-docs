package com.biducollab.docs.collaboration;

import com.biducollab.docs.operation.EditOperation;

public class DocumentClient implements DocumentObserver {

    private String userId;

    public DocumentClient(String userId) {
        this.userId = userId;
    }

    @Override
    public void onDocumentUpdated(EditOperation operation) {

        // Receive remote edit
        System.out.println(
                "User " + userId
                        + " received operation: "
                        + operation.getOperationId()
        );
    }

    public String getUserId() {
        return userId;
    }
}