package com.biducollab.docs.model.element;

public class TableCell {

    private String cellId;

    private String text;

    public TableCell(
            String cellId,
            String text) {

        this.cellId = cellId;
        this.text = text;
    }

    public String getCellId() {
        return cellId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}