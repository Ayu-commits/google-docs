package com.biducollab.docs.collaboration;

import com.biducollab.docs.operation.EditOperation;

import java.util.ArrayList;
import java.util.List;

public class CollaborationSession {

    private String documentId;

    // Connected clients for this document
    private List<DocumentObserver> observers;

    public CollaborationSession(String documentId) {
        this.documentId = documentId;
        this.observers = new ArrayList<>();
    }

    // Add a connected user
    public void addObserver(DocumentObserver observer) {
        observers.add(observer);
    }

    // Remove a disconnected user
    public void removeObserver(DocumentObserver observer) {
        observers.remove(observer);
    }

    // Send operation to all connected users
    public void broadcast(EditOperation operation) {

        for (DocumentObserver observer : observers) {

            // Don't send the same edit back to sender
            if (observer instanceof DocumentClient) {

                DocumentClient client =
                        (DocumentClient) observer;

                if (client.getUserId()
                        .equals(operation.getUserId())) {
                    continue;
                }
            }

            observer.onDocumentUpdated(operation);
        }
    }

    public String getDocumentId() {
        return documentId;
    }
}
/*
User A
  |
  v
InsertTextOperation
  |
  v
CollaborationSession.broadcast()
  |
  ├── User A → Skip
  ├── User B → Notify
  └── User C → Notify
 */