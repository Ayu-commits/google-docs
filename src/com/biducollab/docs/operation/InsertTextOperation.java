package com.biducollab.docs.operation;

import com.biducollab.docs.model.Document;
import com.biducollab.docs.model.element.DocumentElement;
import com.biducollab.docs.model.element.Paragraph;

import java.time.Instant;

public class InsertTextOperation implements EditOperation {

    private final String operationId;

    private final String documentId;

    private final String userId;

    private final int baseVersion;

    private final String elementId;

    private int position;

    private final String text;

    // Assigned by OperationProcessor when the operation reaches the server.
    private Instant serverTimestamp;

    public InsertTextOperation(
            String operationId,
            String documentId,
            String userId,
            int baseVersion,
            String elementId,
            int position,
            String text) {

        this.operationId = operationId;
        this.documentId = documentId;
        this.userId = userId;
        this.baseVersion = baseVersion;
        this.elementId = elementId;
        this.position = position;
        this.text = text;
    }

    @Override
    public String getOperationId() {
        return operationId;
    }

    @Override
    public String getDocumentId() {
        return documentId;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public int getBaseVersion() {
        return baseVersion;
    }

    @Override
    public Instant getServerTimestamp() {
        return serverTimestamp;
    }

    @Override
    public void setServerTimestamp(Instant timestamp) {
        this.serverTimestamp = timestamp;
    }

    public String getElementId() {
        return elementId;
    }

    public int getPosition() {
        return position;
    }

    public String getText() {
        return text;
    }

    @Override
    public void execute(Document document) {

        Paragraph paragraph = findParagraph(document);

        if (paragraph == null) {
            throw new IllegalArgumentException(
                    "Paragraph not found: " + elementId
            );
        }

        String currentText = paragraph.getText();

        if (position < 0 || position > currentText.length()) {
            throw new IllegalArgumentException(
                    "Invalid insert position: " + position
            );
        }

        String updatedText =
                currentText.substring(0, position)
                        + text
                        + currentText.substring(position);

        paragraph.setText(updatedText);

        document.incrementVersion();
    }

    @Override
    public void undo(Document document) {

        Paragraph paragraph = findParagraph(document);

        if (paragraph == null) {
            throw new IllegalArgumentException(
                    "Paragraph not found: " + elementId
            );
        }

        String currentText = paragraph.getText();

        int endPosition = position + text.length();

        if (position < 0
                || endPosition > currentText.length()) {

            throw new IllegalArgumentException(
                    "Cannot undo insert operation"
            );
        }

        String updatedText =
                currentText.substring(0, position)
                        + currentText.substring(endPosition);

        paragraph.setText(updatedText);

        document.incrementVersion();
    }

    private Paragraph findParagraph(Document document) {

        for (DocumentElement element : document.getElements()) {

            if (element instanceof Paragraph
                    && element.getElementId().equals(elementId)) {

                return (Paragraph) element;
            }
        }

        return null;
    }
}

/*
Example:
  Paragraph: "HelloWorld"
  InsertTextOperation(position=5, text=" ")

  execute():
    substring(0,5) + " " + substring(5) = "Hello World"

  undo():
    Removes inserted text at position 5..6 → "HelloWorld"
 */