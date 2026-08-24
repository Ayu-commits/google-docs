package com.biducollab.docs.model.element;

public class ImageBlock implements DocumentElement {

    private String elementId;

    private String imageUrl;

    private String altText;

    private int width;

    private int height;

    public ImageBlock(
            String elementId,
            String imageUrl,
            String altText,
            int width,
            int height) {

        this.elementId = elementId;
        this.imageUrl = imageUrl;
        this.altText = altText;
        this.width = width;
        this.height = height;
    }

    @Override
    public String getElementId() {
        return elementId;
    }

    @Override
    public ElementType getType() {
        return ElementType.IMAGE;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public <T> T accept(DocumentElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}