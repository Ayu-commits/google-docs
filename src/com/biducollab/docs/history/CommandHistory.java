package com.biducollab.docs.history;

import com.biducollab.docs.model.Document;
import com.biducollab.docs.operation.EditOperation;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandHistory {

    // Stores operations that can be undone
    private final Deque<EditOperation> undoStack = new ArrayDeque<>();

    // Stores operations that can be redone
    private final Deque<EditOperation> redoStack = new ArrayDeque<>();

    // Execute operation and save it for undo
    public void execute(
            EditOperation operation,
            Document document) {

        operation.execute(document);

        undoStack.push(operation);

        // New action clears redo history
        redoStack.clear();
    }

    // Undo the latest operation
    public void undo(Document document) {

        if (undoStack.isEmpty()) {
            System.out.println("Nothing to undo");
            return;
        }

        EditOperation operation = undoStack.pop();

        operation.undo(document);

        redoStack.push(operation);
    }

    // Redo the latest undone operation
    public void redo(Document document) {

        if (redoStack.isEmpty()) {
            System.out.println("Nothing to redo");
            return;
        }

        EditOperation operation = redoStack.pop();

        operation.execute(document);

        undoStack.push(operation);
    }
}
/*
Flow
User types "Hello"
        |
        v
InsertTextOperation
        |
        v
CommandHistory.execute()
        |
        ├── operation.execute()
        |
        └── Push into Undo Stack
Undo
Undo Stack
    |
    v
Pop operation
    |
operation.undo()
    |
    v
Redo Stack
Redo
Redo Stack
    |
    v
Pop operation
    |
operation.execute()
    |
    v
Undo Stack
 */