package com.biducollab.docs.permission;

import com.biducollab.docs.model.Document;
import com.biducollab.docs.model.DocumentPermission;
import com.biducollab.docs.model.PermissionType;

public class AuthorizationService {

    // Check if user can edit the document
    public boolean canEdit(
            Document document,
            String userId) {

        for (DocumentPermission permission
                : document.getPermissions()) {

            if (permission.getUserId().equals(userId)) {

                PermissionType permissionType =
                        permission.getPermissionType();

                return permissionType == PermissionType.OWNER
                        || permissionType == PermissionType.EDITOR;
            }
        }

        // No permission found
        return false;
    }

    // Check if user can view the document
    public boolean canView(
            Document document,
            String userId) {

        for (DocumentPermission permission
                : document.getPermissions()) {

            if (permission.getUserId().equals(userId)) {

                return true;
            }
        }

        return false;
    }
}