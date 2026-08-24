package com.biducollab.docs.presence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Manages active users in a document
public class PresenceService {

    private final Map<String, List<UserPresence>>
            usersByDocument = new HashMap<>();

    // User joins a document
    public void joinDocument(
            String documentId,
            String userId) {

        UserPresence presence =
                new UserPresence(
                        userId,
                        documentId
                );

        usersByDocument
                .computeIfAbsent(
                        documentId,
                        key -> new ArrayList<>()
                )
                .add(presence);
    }

    // User leaves a document
    public void leaveDocument(
            String documentId,
            String userId) {

        List<UserPresence> users =
                usersByDocument.get(documentId);

        if (users == null) {
            return;
        }

        users.removeIf(
                presence ->
                        presence.getUserId()
                                .equals(userId)
        );
    }

    // Get all active users
    public List<UserPresence> getActiveUsers(
            String documentId) {

        return new ArrayList<>(
                usersByDocument.getOrDefault(
                        documentId,
                        new ArrayList<>()
                )
        );
    }

    // Update user activity
    public void updateActivity(
            String documentId,
            String userId) {

        List<UserPresence> users =
                usersByDocument.get(documentId);

        if (users == null) {
            return;
        }

        for (UserPresence presence : users) {

            if (presence.getUserId()
                    .equals(userId)) {

                presence.updateActivity();
                return;
            }
        }
    }
}
/*
User opens document
        |
        v
PresenceService.joinDocument()
        |
        v
UserPresence created
        |
        v
User starts editing
        |
        +-----------------------+
        |                       |
        v                       v
DocumentSyncManager       PresenceService
        |                       |
        v                       v
Online / Offline          updateActivity()
 */