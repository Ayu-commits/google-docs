package com.biducollab.docs.collaboration;

import com.biducollab.docs.operation.EditOperation;

public interface DocumentObserver {

    // Called when another user edits the document
    void onDocumentUpdated(EditOperation operation);
}