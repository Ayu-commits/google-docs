package com.biducollab.docs.model.element;

import java.util.ArrayList;
import java.util.List;

public class Table implements DocumentElement {

    private String elementId;

    private List<TableRow> rows;

    public Table(String elementId) {
        this.elementId = elementId;
        this.rows = new ArrayList<>();
    }

    @Override
    public String getElementId() {
        return elementId;
    }

    @Override
    public ElementType getType() {
        return ElementType.TABLE;
    }

    public List<TableRow> getRows() {
        return rows;
    }

    public void addRow(TableRow row) {
        rows.add(row);
    }

    public void removeRow(int index) {
        rows.remove(index);
    }

    @Override
    public <T> T accept(DocumentElementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
/*
DocumentElement
       ▲
       │
 ┌─────┼──────────┬──────────┐
 │     │          │          │
Paragraph Heading ImageBlock Table
                                  │
                               TableRow
                                  │
                               TableCell
 */