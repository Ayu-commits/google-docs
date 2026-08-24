package com.biducollab.docs.collaboration;

import com.biducollab.docs.operation.EditOperation;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CollaborationSession {

    private final String documentId;

    // CopyOnWriteArrayList allows safe concurrent iteration during broadcast.
    private final List<DocumentObserver> observers =
            new CopyOnWriteArrayList<>();

    public CollaborationSession(String documentId) {
        this.documentId = documentId;
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