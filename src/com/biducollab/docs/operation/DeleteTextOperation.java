package com.biducollab.docs.operation;

import com.biducollab.docs.model.Document;
import com.biducollab.docs.model.element.DocumentElement;
import com.biducollab.docs.model.element.Paragraph;

public class DeleteTextOperation implements EditOperation {

    private String operationId;

    private String documentId;

    private String userId;

    private int baseVersion;

    private String elementId;

    private int position;

    private int length;

    // This will be used for undo
    private String deletedText;

    public DeleteTextOperation(
            String operationId,
            String documentId,
            String userId,
            int baseVersion,
            String elementId,
            int position,
            int length) {

        this.operationId = operationId;
        this.documentId = documentId;
        this.userId = userId;
        this.baseVersion = baseVersion;
        this.elementId = elementId;
        this.position = position;
        this.length = length;
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

    public String getElementId() {
        return elementId;
    }

    public int getPosition() {
        return position;
    }

    public int getLength() {
        return length;
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

        int endPosition = position + length;

        if (position < 0
                || endPosition > currentText.length()) {

            throw new IllegalArgumentException(
                    "Invalid delete range"
            );
        }

        // Save deleted text for undo
        deletedText = currentText.substring(
                position,
                endPosition
        );

        String updatedText =
                currentText.substring(0, position)
                        + currentText.substring(endPosition);

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

        if (deletedText == null) {
            throw new IllegalStateException(
                    "Cannot undo before executing delete operation"
            );
        }

        String currentText = paragraph.getText();

        if (position < 0
                || position > currentText.length()) {

            throw new IllegalArgumentException(
                    "Invalid restore position"
            );
        }

        String updatedText =
                currentText.substring(0, position)
                        + deletedText
                        + currentText.substring(position);

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
Suppose paragraph:

Hello Beautiful World

Aur operation:

position = 6
length = 10

Yahan "Beautiful " delete hoga.

Execute
Hello Beautiful World
      |---------|
         delete


Result:


Hello World

Internally:

deletedText = "Beautiful "
Undo
Hello World
      |
      position = 6


Insert deletedText back


Hello Beautiful World
 */