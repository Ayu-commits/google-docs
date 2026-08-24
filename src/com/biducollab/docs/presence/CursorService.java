package com.biducollab.docs.presence;

import java.util.HashMap;
import java.util.Map;

// Manages cursor positions of users
public class CursorService {

    private final Map<String, Map<String, CursorPosition>>
            cursorsByDocument = new HashMap<>();

    // Update user's cursor position
    public void updateCursor(
            String documentId,
            String userId,
            int position) {

        Map<String, CursorPosition> documentCursors =
                cursorsByDocument.computeIfAbsent(
                        documentId,
                        key -> new HashMap<>()
                );

        CursorPosition cursorPosition =
                documentCursors.get(userId);

        if (cursorPosition == null) {

            cursorPosition =
                    new CursorPosition(
                            userId,
                            documentId,
                            position
                    );

            documentCursors.put(
                    userId,
                    cursorPosition
            );

        } else {

            cursorPosition.updatePosition(position);
        }
    }

    // Get user's cursor position
    public CursorPosition getCursor(
            String documentId,
            String userId) {

        Map<String, CursorPosition> documentCursors =
                cursorsByDocument.get(documentId);

        if (documentCursors == null) {
            return null;
        }

        return documentCursors.get(userId);
    }

    // Remove cursor when user leaves
    public void removeCursor(
            String documentId,
            String userId) {

        Map<String, CursorPosition> documentCursors =
                cursorsByDocument.get(documentId);

        if (documentCursors == null) {
            return;
        }

        documentCursors.remove(userId);
    }
}