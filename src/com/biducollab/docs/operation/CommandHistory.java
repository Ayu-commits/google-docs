package com.biducollab.docs.operation;

import com.biducollab.docs.model.Document;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Per-user undo/redo stack for document edits.
 *
 * <p>Two usage modes:
 * <ul>
 *   <li>{@link #execute} — applies the operation to the document AND records it
 *       (use when the caller has not yet applied the operation).</li>
 *   <li>{@link #record} — records an already-applied, OT-transformed operation
 *       without executing it again (use from {@code DocumentCollaborationManager}
 *       where {@code OperationProcessor} has already called {@code execute()}).</li>
 * </ul>
 */
public class CommandHistory {

    // Stores operations that can be undone.
    private final Deque<EditOperation> undoStack = new ArrayDeque<>();

    // Stores operations that can be redone.
    private final Deque<EditOperation> redoStack = new ArrayDeque<>();

    /**
     * Apply the operation to the document and push it onto the undo stack.
     * Clears the redo stack (a new edit invalidates any redoable future).
     */
    public void execute(
            EditOperation operation,
            Document document) {

        operation.execute(document);
        undoStack.push(operation);
        redoStack.clear();
    }

    /**
     * Record an already-applied operation for future undo without re-executing it.
     * Use this when the operation has been executed externally (e.g. via OT pipeline).
     * Clears the redo stack for the same reason as {@link #execute}.
     */
    public void record(EditOperation operation) {
        undoStack.push(operation);
        redoStack.clear();
    }

    /**
     * Undo the most recently executed operation.
     * The reversed operation is pushed onto the redo stack.
     */
    public void undo(Document document) {

        if (undoStack.isEmpty()) {
            return;
        }

        EditOperation operation = undoStack.pop();
        operation.undo(document);
        redoStack.push(operation);
    }

    /**
     * Re-apply the most recently undone operation.
     * The operation is pushed back onto the undo stack.
     */
    public void redo(Document document) {

        if (redoStack.isEmpty()) {
            return;
        }

        EditOperation operation = redoStack.pop();
        operation.execute(document);
        undoStack.push(operation);
    }

    /** Returns true when there is at least one operation that can be undone. */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /** Returns true when there is at least one operation that can be redone. */
    public boolean canRedo() {
        return !redoStack.isEmpty();
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
