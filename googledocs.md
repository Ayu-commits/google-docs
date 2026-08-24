Google Docs / Collaborative Document Editor --- System Design Notes

1. Functional Requirements

1.1 Create and manage documents

User can create, open, rename, and delete a document.
A document has an owner and a unique document ID.

1.2 Rich document editing

Users can add and edit: - Paragraphs - Headings - Images - Tables -
Lists
Example:

Document
├── Heading
├── Paragraph
├── Image
├── Table
│    ├── Row
│    │    ├── Cell
│    │    └── Cell
└── List
├── ListItem
└── ListItem

1.3 Real-time collaboration

Multiple users can open the same document.

Changes made by one user should appear to other active users almost
immediately.

Users should see who is currently editing.

Cursor/selection information can also be shared.

1.4 Edit operations

The editor should support operations such as: - Insert text - Delete
text - Add block - Delete block - Format text

Instead of sending the complete document after every keystroke, the
client sends an operation.

Example:

InsertText
documentId = D1
elementId  = P1
position   = 5
text       = "Hello"
baseVersion = 10

1.5 Sharing and permissions

A user can share a document with different permissions:

Permission   Can View   Can Edit   Can Comment   Can Share

OWNER        Yes        Yes        Yes           Yes
EDITOR       Yes        Yes        Yes           Usually No
COMMENTER    Yes        No         Yes           No
VIEWER       Yes        No         No            No

1.6 Version history

Store document versions or snapshots.

Track who made a change and when.

User can inspect or restore an earlier version.

1.7 Undo and redo

Undo the latest local action.

Redo an action that was undone.

Command history is useful for implementing this.

1.8 Offline editing

When the user has no internet: 1. Apply the change locally. 2. Store the
operation in a local pending queue. 3. When connectivity returns,
synchronize pending operations. 4. Resolve conflicts with changes that
happened while the user was offline.

2. Non-Functional Requirements (NFR)

2.1 Low latency

Editing should feel instant.

Typical target:

Edit propagation: roughly 100–200 ms for a good collaboration experience

2.2 High availability

The service should continue working even if some servers fail.

Example target:

99.9%+ availability

2.3 Scalability

The system should support: - Many documents - Many active users - Many
simultaneous collaboration sessions

2.4 Fault tolerance

Failure of one server should not lose the document or end all
collaboration.

2.5 Durability

Committed document data and version history should survive server or
storage failures.

2.6 Consistency

All collaborators should eventually converge to the same logical
document state.

This is especially important when two users edit the same area
concurrently.

2.7 Security

Authenticate users.

Check permissions before accepting edits.

Protect documents in transit and at rest.

3. High-Level Design (HLD)

3.1 Main components

                  +----------------+
                  |    Client A    |
                  | Document Editor|
                  +-------+--------+
                          |
                       WebSocket
                          |
                  +-------v--------+
                  | Collaboration  |
                  | / Realtime API |
                  +-------+--------+
                          |
             +------------+------------+
             |                         |
      +------v------+           +------v-------+
      | Session /   |           | Operation    |
      | Presence    |           | Processor    |
      +------+------+\          +------+-------+
             |                   | Conflict
             |                   | Resolution
             |                   v
             |              +----+-----+
             |              | OT/CRDT  |
             |              +----+-----+
             |                   |
             +---------+---------+
                       |
                 +-----v------+
                 | Document   |
                 | Service    |
                 +-----+------+
                       |
          +------------+-------------+
          |                          |
+------v-------+           +------v--------+
| Document DB  |           | Version Store |
+--------------+           +---------------+

                  +----------------+
                  |    Client B    |
                  | Document Editor|
                  +----------------+

3.2 Edit flow

Suppose User A types Hello.

1. User A types
   |
2. Client creates EditOperation
   |
3. Apply optimistically on local UI
   |
4. Send operation to Collaboration Server
   |
5. Server checks permission
   |
6. Server checks document version
   |
7. If concurrent operations exist:
   |
   v
   OT / CRDT conflict resolution
   |
8. Apply final operation
   |
9. Persist operation / snapshot
   |
10. Broadcast to User B, C, D

3.3 Why WebSocket?

HTTP request-response is not ideal for continuous bidirectional updates.

WebSocket provides: - Persistent connection - Low overhead after
connection establishment - Server can push updates immediately

3.4 Presence service

Presence is separate from document content.

It can maintain:

documentId
userId
cursorPosition
selection
lastHeartbeat

Presence data is usually more ephemeral than document data.

4. Low-Level Design (LLD)

4.1 Core data model

User
|
| owns / has permission
v
Document
|
| contains
v
DocumentElement
|
+----------------+----------------+---------------+
|                |                |               |
Paragraph       Heading          Image            Table
|
v
TableRow
|
v
TableCell

Document
|
+---- DocumentPermission
|
+---- EditOperation
|
+---- DocumentVersion
|
+---- CollaborationSession

4.2 User

User
----
userId
name
email

Relationship:

One User
|
+---- owns many Documents
|
+---- has permissions on many Documents

4.3 Document

Document
--------
documentId
title
ownerId
currentVersion
elements

Relationship:

Document 1 ------ * DocumentElement
Document 1 ------ * DocumentPermission
Document 1 ------ * EditOperation
Document 1 ------ * DocumentVersion

4.4 DocumentElement

Common abstraction:

public interface DocumentElement {
String getElementId();
BlockType getType();
}

Possible implementations:

DocumentElement
|
+---- Paragraph
|
+---- Heading
|
+---- ImageBlock
|
+---- Table
|
+---- DocumentList

This makes the document extensible.

Tomorrow we can add:

VideoBlock
CodeBlock
EquationBlock
CommentBlock

without changing the core Document class.

4.5 Table relationships

Document
|
v
Table
|
+----- TableRow
|         |
|         +---- TableCell
|         +---- TableCell
|
+----- TableRow
|
+---- TableCell

Cardinality:

Table      1 -> many TableRows
TableRow   1 -> many TableCells

4.6 Permission model

DocumentPermission
------------------
documentId
userId
permissionType

Relationship:

User  * ----- * Document

Implemented using:

DocumentPermission

This is a many-to-many relationship with additional metadata.

4.7 EditOperation

EditOperation
-------------
operationId
documentId
userId
baseVersion
operationType
timestamp

Implementations:

EditOperation
|
+---- InsertTextOperation
+---- DeleteTextOperation
+---- AddBlockOperation
+---- DeleteBlockOperation
+---- FormatTextOperation

Example:

InsertTextOperation
-------------------
elementId
position
text

This is better than sending the entire document after every edit.

4.8 DocumentVersion

DocumentVersion
---------------
documentId
version
modifiedBy
modifiedAt
snapshot

Conceptually:

Document
|
+---- Version 1
|
+---- Version 2
|
+---- Version 3

For a production-scale design, systems often combine: - Operation log
for efficient incremental changes - Periodic snapshots to avoid
replaying a huge operation history

4.9 CollaborationSession

CollaborationSession
--------------------
documentId
connectedUsers
observers / clients

It manages active collaborators for one document.

Document D1

        CollaborationSession
             /      |      \
            /       |       \
        User A    User B   User C

When an operation is accepted:

broadcast(operation)

4.10 OfflineOperationQueue

OfflineOperationQueue
---------------------
pendingOperations

Flow:

OFFLINE

User types
|
v
Create operation
|
v
Apply locally
|
v
Add to pending queue

ONLINE AGAIN
|
v
Fetch / receive server changes
|
v
Transform pending operations
|
v
Send operations
|
v
Clear acknowledged operations

5. Design Patterns Used and Why

5.1 Command Pattern

Classes

EditOperation
InsertTextOperation
DeleteTextOperation
AddBlockOperation
DeleteBlockOperation
FormatTextOperation

Why?

Every user action becomes an object.

User Action
|
v
EditOperation
|
+---- execute()
|
+---- undo()

Benefits: - Undo - Redo - Operation history - Synchronization -
Logging - Replay

Example:

public interface EditOperation {
void execute(Document document);
void undo(Document document);
}

5.2 Observer Pattern

Classes

DocumentObserver
DocumentClient
CollaborationSession

Why?

One document update must notify multiple collaborators.

                CollaborationSession
                    |
       +------------+------------+
       |            |            |
       v            v            v
    Client A      Client B      Client C

When a change occurs:

session.broadcast(operation)

5.3 Strategy Pattern

Classes

ConflictResolutionStrategy
|
+---- OperationalTransformationStrategy
|
+---- CRDTStrategy

Why?

Conflict resolution can change without changing the collaboration
service.

public interface ConflictResolutionStrategy {
EditOperation resolve(
EditOperation incoming,
List<EditOperation> concurrentOperations
);
}

We can switch:

OT
|
+--> CRDT

depending on requirements.

5.4 State Pattern

Classes

SyncState
|
+---- OnlineState
+---- OfflineState
+---- SyncingState

Why?

The same edit behaves differently depending on network state.

ONLINE
Edit -> Send immediately

OFFLINE
Edit -> Store locally

SYNCING
Edit -> Queue while synchronization is happening

5.5 Factory Pattern

Class

DocumentElementFactory

Why?

Document elements may be created dynamically.

PARAGRAPH -> new Paragraph()
HEADING   -> new Heading()
IMAGE     -> new ImageBlock()

Instead of spreading object creation logic throughout the code.

5.6 Memento Pattern

Classes

DocumentSnapshot
DocumentVersion
VersionHistory

Why?

Version history needs to preserve an earlier state.

Current Document
|
| create snapshot
v
DocumentSnapshot
|
v
VersionHistory

Useful for: - Version history - Restore - Recovery

5.7 Composite Pattern

The document is composed of different elements.

Document
|
+---- Paragraph
+---- Heading
+---- Table
|       |
|       +---- Rows
|                |
|                +---- Cells
|
+---- List
|
+---- ListItems

The common DocumentElement abstraction allows different element types
to be handled polymorphically.

6. Important OT Example

Suppose the document contains:

HelloWorld

Both users edit at position 5.

User A:

Insert " "

User B:

Insert "Beautiful "

Without conflict resolution:

Both think position = 5

But after one operation is applied, positions change.

Operational Transformation transforms the second operation relative to
the first.

Conceptually:

A: Insert(5, " ")

B: Insert(5, "Beautiful ")

Transform B based on A

B': Insert(6, "Beautiful ")

Final result can become:

Hello Beautiful World

A real OT implementation must also define deterministic tie-breaking for
simultaneous inserts at the same position.

7. Interview Questions That Can Be Asked

Q1. Why not store the entire document as one String?

Answer: Because modern documents contain different block types and
hierarchical structures.

String
X Paragraph structure
X Image
X Table
X List
X Rich formatting

A structured model is more extensible.

Q2. How will you support a new block such as Video?

Answer:

VideoBlock implements DocumentElement

The document model does not need to change significantly.

Q3. How does undo/redo work?

Use two stacks:

Undo Stack
Redo Stack

Flow:

Execute Operation
|
v
Push to Undo Stack
Clear Redo Stack

Undo:

Pop Undo
|
operation.undo()
|
Push to Redo

Redo:

Pop Redo
|
operation.execute()
|
Push to Undo

Q4. Two users edit the same position. How do you handle it?

Use: - Operational Transformation, or - CRDT

Also define: - Server ordering - Base document version - Operation ID -
Deterministic tie-breaker

Q5. What happens if the user is offline?

Edit locally
|
Queue operation
|
Internet returns
|
Synchronize
|
Resolve conflicts

Q6. How do you scale collaboration?

Potential design:

Load Balancer
|
+---- Collaboration Server 1
+---- Collaboration Server 2
+---- Collaboration Server 3

Possible supporting components: - Sticky routing or session mapping -
Pub/Sub for cross-server broadcasts - Distributed cache for active
session metadata

Q7. How do you prevent an unauthorized user from editing?

Before applying an operation:

Operation
|
v
Authentication
|
v
Authorization
|
+--> VIEWER -> Reject
|
+--> COMMENTER -> Reject edit
|
+--> EDITOR -> Allow
|
+--> OWNER -> Allow

Q8. How would version history scale?

Do not necessarily save a full copy after every keystroke.

Use:

Operation Log
+
Periodic Snapshots

To reconstruct a version:

Snapshot
+
Operations after snapshot
=
Required version

Q9. How do you ensure durability?

Possible production approach: 1. Accept operation. 2. Persist or
replicate according to durability requirements. 3. Acknowledge the
operation. 4. Broadcast to collaborators.

The exact ordering is a trade-off between latency and durability
guarantees.

Q10. What is the difference between OT and CRDT?

OT

Operation A + Operation B
|
Transform
|
v
Converged result

Usually requires reasoning about operation ordering and transformation
functions.

CRDT

Each replica has a data structure designed so that concurrent changes
can merge and converge.

Simplified comparison:

OT                                  CRDT

Transform operations                Merge replica states/operations

Common in collaborative editors     Strong fit for distributed/offline
scenarios

Central coordination can simplify   Naturally supports replicas merging
ordering

The right choice depends on product requirements.

8. How to Explain the Complete Design in an Interview

A good answer flow:

Step 1 --- Clarify scope

Say:

I will focus on document editing, real-time collaboration,
permissions, version history, offline edits, and conflict resolution.

Step 2 --- Identify entities

User
Document
DocumentElement
EditOperation
Permission
Version
CollaborationSession

Step 3 --- Model the document

Document
|
+---- List<DocumentElement>

Step 4 --- Model edits

EditOperation
|
+---- Insert
+---- Delete
+---- AddBlock
+---- DeleteBlock
+---- Format

Explain Command Pattern.

Step 5 --- Add collaboration

Operation
|
v
CollaborationSession
|
v
Notify connected users

Explain Observer Pattern.

Step 6 --- Add conflict resolution

Incoming Operation
+
Concurrent Operations
|
v
ConflictResolutionStrategy
|
+---- OT
|
+---- CRDT

Explain Strategy Pattern.

Step 7 --- Add offline support

Online  -> Send
Offline -> Queue
Syncing -> Synchronize

Explain State Pattern.

9. Final Architecture Summary

                         USER
                          |
                          v
                    Document Client
                          |
                  +-------+-------+
                  | Local Document|
                  | + Undo/Redo   |
                  | + Offline Queue|
                  +-------+-------+
                          |
                       WebSocket
                          |
                          v
                Collaboration Service
                          |
          +---------------+----------------+
          |               |                |
          v               v                v
   Authorization   Conflict Resolution   Presence
   |               |
   |               +---- OT
   |               +---- CRDT
   |
   v
   Operation Processor
   |
   +-----------+-----------+
   |                       |
   v                       v
   Document Storage        Version Storage

10. One-Minute Interview Summary

I would model the document as a collection of extensible
DocumentElement objects so that paragraphs, headings, tables, lists,
and future block types can be supported cleanly. Every user change is
represented as an EditOperation, which follows the Command Pattern
and naturally supports execution, undo, redo, and operation history.
Active collaborators are connected through a CollaborationSession,
using an Observer-style abstraction to broadcast accepted changes.
Concurrent edits are handled behind a ConflictResolutionStrategy,
allowing OT or CRDT implementations to be selected without changing
the rest of the collaboration flow. For offline support, operations
are applied locally and stored in an offline queue, with behavior
depending on the current synchronization state. Permissions, version
history, and periodic snapshots complete the core design.