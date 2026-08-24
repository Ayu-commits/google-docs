package com.biducollab.docs.comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Jab user document par comment add kare, kisi existing comment ka reply de,
comment resolve/reopen kare, ya document ke comments dekhna chahe, tab ye service use hogi.
 */
public class CommentService {

    // Stores comments document-wise
    private final Map<String, List<Comment>> commentsByDocument =
            new HashMap<>();

    // Add a new comment
    public void addComment(Comment comment) {

        commentsByDocument
                .computeIfAbsent(
                        comment.getDocumentId(),
                        key -> new ArrayList<>()
                )
                .add(comment);
    }

    // Add reply to an existing comment
    public void addReply(
            String documentId,
            String parentCommentId,
            Comment reply) {

        Comment parentComment =
                findComment(
                        documentId,
                        parentCommentId
                );

        if (parentComment == null) {
            throw new IllegalArgumentException(
                    "Comment not found"
            );
        }

        parentComment.addReply(reply);
    }

    // Resolve a comment
    public void resolveComment(
            String documentId,
            String commentId) {

        Comment comment =
                findComment(
                        documentId,
                        commentId
                );

        if (comment == null) {
            throw new IllegalArgumentException(
                    "Comment not found"
            );
        }

        comment.resolve();
    }

    // Reopen a resolved comment
    public void reopenComment(
            String documentId,
            String commentId) {

        Comment comment =
                findComment(
                        documentId,
                        commentId
                );

        if (comment == null) {
            throw new IllegalArgumentException(
                    "Comment not found"
            );
        }

        comment.reopen();
    }

    // Get all comments for a document
    public List<Comment> getComments(
            String documentId) {

        return new ArrayList<>(
                commentsByDocument.getOrDefault(
                        documentId,
                        new ArrayList<>()
                )
        );
    }

    // Find comment by id
    private Comment findComment(
            String documentId,
            String commentId) {

        List<Comment> comments =
                commentsByDocument.get(documentId);

        if (comments == null) {
            return null;
        }

        for (Comment comment : comments) {

            if (comment.getCommentId()
                    .equals(commentId)) {

                return comment;
            }
        }

        return null;
    }
}
/*
User A
  |
  | Add Comment
  v
CommentService
  |
  v
commentsByDocument
  |
  └── doc-1
       |
       ├── Comment 1
       │      |
       │      └── Reply 1
       |
       └── Comment 2
 */