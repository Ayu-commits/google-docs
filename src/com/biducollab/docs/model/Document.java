package com.biducollab.docs.model;

import com.biducollab.docs.model.element.DocumentElement;

import java.util.ArrayList;
import java.util.List;

public class Document {

    private String documentId;

    private String title;

    private String ownerId;

    private int currentVersion;

    private List<DocumentElement> elements;

    private List<DocumentPermission> permissions;

    public Document(
            String documentId,
            String title,
            String ownerId) {

        this.documentId = documentId;
        this.title = title;
        this.ownerId = ownerId;
        this.currentVersion = 1;

        this.elements = new ArrayList<>();
        this.permissions = new ArrayList<>();

        // Owner automatically gets OWNER permission
        this.permissions.add(
                new DocumentPermission(
                        documentId,
                        ownerId,
                        PermissionType.OWNER
                )
        );
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public List<DocumentElement> getElements() {
        return elements;
    }

    public List<DocumentPermission> getPermissions() {
        return permissions;
    }

    public void addElement(DocumentElement element) {
        elements.add(element);
    }

    public void removeElement(int index) {
        elements.remove(index);
    }

    public void addPermission(
            DocumentPermission permission) {

        permissions.add(permission);
    }

    public void incrementVersion() {
        currentVersion++;
    }
}

/*
                    Document
                       |
        +--------------+--------------+
        |                             |
        v                             v
DocumentElement               DocumentPermission
     List                           List
        |                             |
        v                             v
Paragraph / Heading              User access
Image / Table                    OWNER / EDITOR
                                 COMMENTER / VIEWER
 */