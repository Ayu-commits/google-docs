package com.biducollab.docs.permission;

import com.biducollab.docs.model.Document;
import com.biducollab.docs.model.PermissionType;

// Shares document and manages user access
public class DocumentSharingService {

    private final DocumentPermissionService permissionService;

    public DocumentSharingService(
            DocumentPermissionService permissionService) {

        this.permissionService = permissionService;
    }

    // Share document with a user
    public void shareDocument(
            Document document,
            String userId,
            PermissionType permissionType) {

        permissionService.addPermission(
                document,
                userId,
                permissionType
        );

        System.out.println(
                "Document shared with user: " + userId
        );
    }

    // Change shared user's permission
    public void changePermission(
            Document document,
            String userId,
            PermissionType permissionType) {

        permissionService.updatePermission(
                document,
                userId,
                permissionType
        );

        System.out.println(
                "Permission updated for user: " + userId
        );
    }

    // Remove user's document access
    public void removeAccess(
            Document document,
            String userId) {

        permissionService.removePermission(
                document,
                userId
        );

        System.out.println(
                "Access removed for user: " + userId
        );
    }
}