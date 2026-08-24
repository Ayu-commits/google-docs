package com.biducollab.docs.model;

public class DocumentPermission {

    private String documentId;

    private String userId;

    private PermissionType permissionType;

    public DocumentPermission(
            String documentId,
            String userId,
            PermissionType permissionType) {

        this.documentId = documentId;
        this.userId = userId;
        this.permissionType = permissionType;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(
            PermissionType permissionType) {

        this.permissionType = permissionType;
    }
}

/*
User                          Document
 U1 -------------------------> D1
  \                             /
   \                           /
    ------ DocumentPermission
             |
             | documentId = D1
             | userId = U1
             | permission = EDITOR
 */