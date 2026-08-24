package com.biducollab.docs.operation.conflict;

import com.biducollab.docs.operation.DeleteTextOperation;
import com.biducollab.docs.operation.EditOperation;
import com.biducollab.docs.operation.InsertTextOperation;

import java.util.List;

public class OperationalTransformationStrategy
        implements ConflictResolutionStrategy {

    @Override
    public EditOperation resolve(
            EditOperation incomingOperation,
            List<EditOperation> concurrentOperations) {

        EditOperation transformedOperation = incomingOperation;

        // Transform against all concurrent operations
        for (EditOperation concurrentOperation : concurrentOperations) {

            transformedOperation = transform(
                    transformedOperation,
                    concurrentOperation
            );
        }

        return transformedOperation;
    }

    private EditOperation transform(
            EditOperation incoming,
            EditOperation concurrent) {

        // Insert vs Insert
        if (incoming instanceof InsertTextOperation
                && concurrent instanceof InsertTextOperation) {

            return transformInsertAgainstInsert(
                    (InsertTextOperation) incoming,
                    (InsertTextOperation) concurrent
            );
        }

        // Insert vs Delete
        if (incoming instanceof InsertTextOperation
                && concurrent instanceof DeleteTextOperation) {

            return transformInsertAgainstDelete(
                    (InsertTextOperation) incoming,
                    (DeleteTextOperation) concurrent
            );
        }

        // Delete vs Insert
        if (incoming instanceof DeleteTextOperation
                && concurrent instanceof InsertTextOperation) {

            return transformDeleteAgainstInsert(
                    (DeleteTextOperation) incoming,
                    (InsertTextOperation) concurrent
            );
        }

        // Delete vs Delete
        if (incoming instanceof DeleteTextOperation
                && concurrent instanceof DeleteTextOperation) {

            return transformDeleteAgainstDelete(
                    (DeleteTextOperation) incoming,
                    (DeleteTextOperation) concurrent
            );
        }

        return incoming;
    }

    private EditOperation transformInsertAgainstInsert(
            InsertTextOperation incoming,
            InsertTextOperation concurrent) {

        int newPosition = incoming.getPosition();

        // Shift position if concurrent insert is before it
        if (concurrent.getPosition() < incoming.getPosition()) {

            newPosition += concurrent.getText().length();
        }

        // Resolve same-position inserts consistently using operation ID ordering
        else if (concurrent.getPosition()
                == incoming.getPosition()
                && concurrent.getOperationId()
                .compareTo(incoming.getOperationId()) < 0) {

            newPosition += concurrent.getText().length();
        }

        return createInsertOperation(
                incoming,
                newPosition
        );
    }

    private EditOperation transformInsertAgainstDelete(
            InsertTextOperation incoming,
            DeleteTextOperation concurrent) {

        int newPosition = incoming.getPosition();
        int deleteStart = concurrent.getPosition();
        int deleteEnd =
                deleteStart + concurrent.getLength();

        // Delete happened completely before insert
        if (deleteEnd <= incoming.getPosition()) {

            newPosition -= concurrent.getLength();
        }

        // Insert position was inside deleted range
        else if (deleteStart < incoming.getPosition()) {

            newPosition = deleteStart;
        }

        return createInsertOperation(
                incoming,
                newPosition
        );
    }

    private EditOperation transformDeleteAgainstInsert(
            DeleteTextOperation incoming,
            InsertTextOperation concurrent) {

        int newPosition = incoming.getPosition();

        // Insert happened before delete position
        if (concurrent.getPosition() <= incoming.getPosition()) {

            newPosition += concurrent.getText().length();
        }

        return createDeleteOperation(
                incoming,
                newPosition,
                incoming.getLength()
        );
    }

    private EditOperation transformDeleteAgainstDelete(
            DeleteTextOperation incoming,
            DeleteTextOperation concurrent) {

        int newPosition = incoming.getPosition();

        int incomingStart = incoming.getPosition();
        int incomingEnd =
                incomingStart + incoming.getLength();

        int concurrentStart = concurrent.getPosition();
        int concurrentEnd =
                concurrentStart + concurrent.getLength();

        // Concurrent delete is completely before incoming delete
        if (concurrentEnd <= incomingStart) {

            newPosition -= concurrent.getLength();
        }

        // Concurrent delete overlaps incoming delete
        else if (concurrentStart < incomingEnd) {

            newPosition =
                    Math.min(incomingStart, concurrentStart);
        }

        return createDeleteOperation(
                incoming,
                newPosition,
                incoming.getLength()
        );
    }

    // Create transformed insert operation
    private InsertTextOperation createInsertOperation(
            InsertTextOperation operation,
            int newPosition) {

        return new InsertTextOperation(
                operation.getOperationId(),
                operation.getDocumentId(),
                operation.getUserId(),
                operation.getBaseVersion(),
                operation.getElementId(),
                newPosition,
                operation.getText()
        );
    }

    // Create transformed delete operation
    private DeleteTextOperation createDeleteOperation(
            DeleteTextOperation operation,
            int newPosition,
            int newLength) {

        return new DeleteTextOperation(
                operation.getOperationId(),
                operation.getDocumentId(),
                operation.getUserId(),
                operation.getBaseVersion(),
                operation.getElementId(),
                newPosition,
                newLength
        );
    }
}
/*
OT example

Initial document: "HelloWorld"
Both users are on the same document version.

User A: Insert " " at position 5
User B: Insert "Beautiful " at position 5

After User A's operation is applied: "Hello World"

User B's operation must now be transformed:
  Incoming position = 5
        ↓
  OperationalTransformationStrategy
        ↓
  Transformed position = 6 (shifted by 1)
  Result: "Hello Beautiful World"

Four transform cases handled:
  Insert vs Insert  → shift position if concurrent is before
  Insert vs Delete  → shift or clamp to deleted range start
  Delete vs Insert  → shift delete position forward
  Delete vs Delete  → shift or clamp overlapping deletes

Flow:
  Incoming Operation
        |
        v
  OperationalTransformationStrategy
        |
        +--> Insert vs Insert
        |
        +--> Insert vs Delete
        |
        +--> Delete vs Insert
        |
        +--> Delete vs Delete
        |
        v
  Transformed Operation
        |
        v
  Apply to latest document
 */
