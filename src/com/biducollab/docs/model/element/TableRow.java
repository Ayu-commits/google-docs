package com.biducollab.docs.model.element;

import java.util.ArrayList;
import java.util.List;

public class TableRow {

    private String rowId;

    private List<TableCell> cells;

    public TableRow(String rowId) {
        this.rowId = rowId;
        this.cells = new ArrayList<>();
    }

    public String getRowId() {
        return rowId;
    }

    public List<TableCell> getCells() {
        return cells;
    }

    public void addCell(TableCell cell) {
        cells.add(cell);
    }

    public void removeCell(int index) {
        cells.remove(index);
    }
}