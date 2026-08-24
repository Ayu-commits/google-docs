package com.biducollab.docs.model.element;

/**
 * Visitor interface for DocumentElement types.
 * Implement this to add new operations over the element hierarchy
 * without modifying existing element classes (Open/Closed Principle).
 *
 * @param <T> the return type produced by each visit method
 */
public interface DocumentElementVisitor<T> {

    T visit(Paragraph paragraph);

    T visit(Heading heading);

    T visit(ImageBlock imageBlock);

    T visit(DocumentList documentList);

    T visit(Table table);
}
