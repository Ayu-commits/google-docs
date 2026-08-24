package com.biducollab.docs.model.element;

public class ListItem {

    private String itemId;
    private String text;

    public ListItem(
            String itemId,
            String text) {

        this.itemId = itemId;
        this.text = text;
    }

    public String getItemId() {
        return itemId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}