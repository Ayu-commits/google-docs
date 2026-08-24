package com.biducollab.docs.comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Comment {

    private final String commentId;
    private final String documentId;
    private final String userId;
    private final String elementId;
    private final String text;

    // Replies to this comment
    private final List<Comment> replies;

    private final LocalDateTime createdAt;

    private boolean resolved;

    public Comment(
            String commentId,
            String documentId,
            String userId,
            String elementId,
            String text) {

        this.commentId = commentId;
        this.documentId = documentId;
        this.userId = userId;
        this.elementId = elementId;
        this.text = text;

        this.replies = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.resolved = false;
    }

    // Add reply to comment
    public void addReply(Comment reply) {
        replies.add(reply);
    }

    // Mark comment as resolved
    public void resolve() {
        this.resolved = true;
    }

    // Reopen resolved comment
    public void reopen() {
        this.resolved = false;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getUserId() {
        return userId;
    }

    public String getElementId() {
        return elementId;
    }

    public String getText() {
        return text;
    }

    public List<Comment> getReplies() {
        return new ArrayList<>(replies);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isResolved() {
        return resolved;
    }
}