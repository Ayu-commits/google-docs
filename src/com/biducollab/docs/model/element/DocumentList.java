package com.biducollab.docs.model.element;

import java.util.ArrayList;
import java.util.List;

public class DocumentList implements DocumentElement {

    private String elementId;

    private List<ListItem> items;

    public DocumentList(String elementId) {
        this.elementId = elementId;
        this.items = new ArrayList<>();
    }

    @Override
    public String getElementId() {
        return elementId;
    }

    @Override
    public ElementType getType() {
        return ElementType.LIST;
    }

    public List<ListItem> getItems() {
        return items;
    }

    public void addItem(ListItem item) {
        items.add(item);
    }

    public void removeItem(int index) {
        items.remove(index);
    }
}