package com.biducollab.docs.history;

import com.biducollab.docs.model.Document;

// Restores a document from an older version
public class DocumentRestoreService {

    private final VersionHistory versionHistory;

    public DocumentRestoreService(
            VersionHistory versionHistory) {

        this.versionHistory = versionHistory;
    }

    // Get a specific version for restoration
    public DocumentSnapshot restoreVersion(
            String documentId,
            int version) {

        DocumentSnapshot snapshot =
                versionHistory.getVersion(
                        documentId,
                        version
                );

        if (snapshot == null) {
            throw new IllegalArgumentException(
                    "Version not found"
            );
        }

        return snapshot;
    }
}