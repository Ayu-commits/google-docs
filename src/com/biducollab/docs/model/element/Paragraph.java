package com.biducollab.docs.model.element;

public class Paragraph implements DocumentElement {

    private String elementId;

    private String text;

    public Paragraph(
            String elementId,
            String text) {

        this.elementId = elementId;
        this.text = text;
    }

    @Override
    public String getElementId() {
        return elementId;
    }

    @Override
    public ElementType getType() {
        return ElementType.PARAGRAPH;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

/*
Document
    |
    v
List<DocumentElement>
    |
    v
Paragraph
    |
    ├── elementId
    └── text
 */