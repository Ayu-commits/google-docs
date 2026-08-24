package com.biducollab.docs.model.element;

public interface DocumentElement {

    String getElementId();

    ElementType getType();

    /**
     * Accept a visitor and dispatch to the correct visit() overload.
     * Each concrete element calls {@code visitor.visit(this)}.
     */
    <T> T accept(DocumentElementVisitor<T> visitor);
}