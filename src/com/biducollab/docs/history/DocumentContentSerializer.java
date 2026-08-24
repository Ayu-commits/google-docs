package com.biducollab.docs.history;

import com.biducollab.docs.model.Document;
import com.biducollab.docs.model.element.DocumentElement;
import com.biducollab.docs.model.element.DocumentList;
import com.biducollab.docs.model.element.Heading;
import com.biducollab.docs.model.element.ImageBlock;
import com.biducollab.docs.model.element.ListItem;
import com.biducollab.docs.model.element.Paragraph;
import com.biducollab.docs.model.element.Table;
import com.biducollab.docs.model.element.TableCell;
import com.biducollab.docs.model.element.TableRow;

public class DocumentContentSerializer {

    // Convert document into snapshot content
    public String serialize(Document document) {

        StringBuilder content = new StringBuilder();

        for (DocumentElement element : document.getElements()) {

            content.append(
                    serializeElement(element)
            );

            content.append("\n");
        }

        return content.toString();
    }

    // Serialize each document element
    private String serializeElement(
            DocumentElement element) {

        if (element instanceof Paragraph) {

            Paragraph paragraph =
                    (Paragraph) element;

            return paragraph.getText();
        }

        if (element instanceof Heading) {

            Heading heading =
                    (Heading) element;

            return heading.getText();
        }

        if (element instanceof ImageBlock) {

            ImageBlock image =
                    (ImageBlock) element;

            return "[IMAGE: "
                    + image.getImageUrl()
                    + "]";
        }

        if (element instanceof DocumentList) {

            return serializeList(
                    (DocumentList) element
            );
        }

        if (element instanceof Table) {

            return serializeTable(
                    (Table) element
            );
        }

        return "";
    }

    // Serialize document list
    private String serializeList(
            DocumentList documentList) {

        StringBuilder result =
                new StringBuilder();

        for (ListItem item
                : documentList.getItems()) {

            result.append("- ")
                    .append(item.getText())
                    .append("\n");
        }

        return result.toString();
    }

    // Serialize table content
    private String serializeTable(
            Table table) {

        StringBuilder result =
                new StringBuilder();

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
/*
Example

Suppose document mein ye hai:
Heading: My Notes
Paragraph:
Hello World

List:
- Java
- LLD

Table:
Name | Age
Bidu | 25

Serializer output approximately:
My Notes
Hello World
- Java
- LLD

Name | Age |

Bidu | 25 |
 */