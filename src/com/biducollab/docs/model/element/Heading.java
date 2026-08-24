package com.biducollab.docs.model.element;


public class Heading implements DocumentElement {

    private String elementId;

    private String text;

    private int level;

    public Heading(
            String elementId,
            String text,
            int level) {

        this.elementId = elementId;
        this.text = text;
        this.level = level;
    }

    @Override
    public String getElementId() {
        return elementId;
    }

    @Override
    public ElementType getType() {
        return ElementType.HEADING;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
