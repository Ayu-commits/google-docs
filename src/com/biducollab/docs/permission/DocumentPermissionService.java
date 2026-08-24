package com.biducollab.docs.permission;

import com.biducollab.docs.model.Document;
import com.biducollab.docs.model.DocumentPermission;
import com.biducollab.docs.model.PermissionType;

public class DocumentPermissionService {

    // Use Case:
    // Manage user permissions for a document

    // Give permission to a user
    public void addPermission(
            Document document,
            String userId,
            PermissionType permissionType) {

        // Remove existing permission if present
        removePermission(document, userId);

        DocumentPermission permission =
                new DocumentPermission(
                        document.getDocumentId(),
                        userId,
                        permissionType
                );

        document.getPermissions()
                .add(permission);
    }

    // Change user's permission
    public void updatePermission(
            Document document,
            String userId,
            PermissionType permissionType) {

        addPermission(
                document,
                userId,
                permissionType
        );
    }

    // Remove user's access
    public void removePermission(
            Document document,
            String userId) {

        document.getPermissions()
                .removeIf(
                        permission ->
                                permission.getUserId()
                                        .equals(userId)
                );
    }

    // Get user's permission
    public PermissionType getPermission(
            Document document,
            String userId) {

        for (DocumentPermission permission
                : document.getPermissions()) {

            if (permission.getUserId()
                    .equals(userId)) {

                return permission.getPermissionType();
            }
        }

        return null;
    }
}