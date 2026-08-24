package com.biducollab.docs.document;

import com.biducollab.docs.model.Document;
import com.biducollab.docs.model.element.DocumentElement;
import com.biducollab.docs.model.element.DocumentElementVisitor;
import com.biducollab.docs.model.element.DocumentList;
import com.biducollab.docs.model.element.Heading;
import com.biducollab.docs.model.element.ImageBlock;
import com.biducollab.docs.model.element.ListItem;
import com.biducollab.docs.model.element.Paragraph;
import com.biducollab.docs.model.element.Table;
import com.biducollab.docs.model.element.TableCell;
import com.biducollab.docs.model.element.TableRow;

/**
 * Converts a Document into a plain-text snapshot string.
 *
 * Implements {@link DocumentElementVisitor} so each element type is handled
 * by a dedicated visit() method instead of a brittle instanceof chain.
 * Adding a new element type only requires adding a new visit() overload here
 * and in the visitor interface — no changes to existing methods.
 *
 * Example output for a document containing Heading + Paragraph + List + Table:
 * <pre>
 *   My Notes
 *   Hello World
 *   - Java
 *   - LLD
 *   Name | Age |
 *   Bidu | 25  |
 * </pre>
 */
public class DocumentContentSerializer
        implements DocumentElementVisitor<String> {

    /**
     * Serialize every element in the document, separated by newlines.
     */
    public String serialize(Document document) {

        StringBuilder content = new StringBuilder();

        for (DocumentElement element : document.getElements()) {
            content.append(element.accept(this));
            content.append("\n");
        }

        return content.toString();
    }

    // ------------------------------------------------------------------ //
    //  DocumentElementVisitor<String> implementation                       //
    // ------------------------------------------------------------------ //

    @Override
    public String visit(Paragraph paragraph) {
        return paragraph.getText();
    }

    @Override
    public String visit(Heading heading) {
        // Prefix with '#' characters matching the heading level (H1–H6).
        String prefix = "#".repeat(Math.max(1, heading.getLevel())) + " ";
        return prefix + heading.getText();
    }

    @Override
    public String visit(ImageBlock imageBlock) {
        return "[IMAGE: " + imageBlock.getImageUrl() + "]";
    }

    @Override
    public String visit(DocumentList documentList) {

        StringBuilder result = new StringBuilder();

        for (ListItem item : documentList.getItems()) {
            result.append("- ")
                  .append(item.getText())
                  .append("\n");
        }

        return result.toString();
    }

    @Override
    public String visit(Table table) {

        StringBuilder result = new StringBuilder();

        for (TableRow row : table.getRows()) {

            for (TableCell cell : row.getCells()) {
                result.append(cell.getText())
                      .append(" | ");
            }

            result.append("\n");
        }

        return result.toString();
    }
}
