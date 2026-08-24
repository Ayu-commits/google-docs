package com.biducollab.docs.service;

import com.biducollab.docs.collaboration.CollaborationSession;
import com.biducollab.docs.history.CommandHistory;
import com.biducollab.docs.model.Document;
import com.biducollab.docs.operation.EditOperation;
import com.biducollab.docs.permission.AuthorizationService;

public class CollaborationService {

    private final CommandHistory commandHistory;
    private final AuthorizationService authorizationService;

    public CollaborationService(
            CommandHistory commandHistory,
            AuthorizationService authorizationService) {

        this.commandHistory = commandHistory;
        this.authorizationService = authorizationService;
    }

    public void applyEdit(
            Document document,
            EditOperation operation,
            CollaborationSession session) {

        // Check edit permission
        if (!authorizationService.canEdit(
                document,
                operation.getUserId())) {

            throw new SecurityException(
                    "User does not have edit permission"
            );
        }

        // Apply and save for undo
        commandHistory.execute(operation, document);

        // Notify other connected users
        session.broadcast(operation);
    }
}
/*
                 CollaborationService
                         |
                         v
                    applyEdit()
                         |
             +-----------+-----------+
             |                       |
             v                       v
      CommandHistory            CollaborationSession
             |                       |
             v                       v
      Execute + Undo             Broadcast

      Ab complete flow

User Edit
   |
   v
CollaborationService.applyEdit()
   |
   v
AuthorizationService.canEdit()
   |
   +---- No → Reject ❌
   |
   +---- Yes
          |
          v
   CommandHistory.execute()
          |
          v
   Operation.execute()
          |
          v
   Document Updated
          |
          v
   CollaborationSession.broadcast()
          |
          +----> User B
          +----> User C
Example

Agar user-1 ke paas VIEWER permission hai:

user-1 tries to edit
        |
        v
AuthorizationService
        |
        v
canEdit() = false
        |
        v
SecurityException ❌

Agar EDITOR hai:

canEdit() = true
       |
       v
Edit execute ✅
       |
       v
Other users notified 🔄
 */