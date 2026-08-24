package com.biducollab.docs.operation;

import com.biducollab.docs.model.Document;
import com.biducollab.docs.model.element.DocumentElement;
import com.biducollab.docs.model.element.Paragraph;

public class InsertTextOperation implements EditOperation {

    private String operationId;

    private String documentId;

    private String userId;

    private int baseVersion;

    private String elementId;

    private int position;

    private String text;

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
Suppose paragraph hai:

HelloWorld

Aur operation:

position = 5
text = " "
execute()
HelloWorld


substring(0, 5)  = Hello
text             = " "
substring(5)     = World


Result:


Hello World
undo()

Undo mein inserted text remove kar denge:

Hello World
     ↑
position = 5
length = 1

Result:

HelloWorld
 */