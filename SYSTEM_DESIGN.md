# Google Docs — Low-Level Design (LLD)
## Complete System Design, Code Flow & Improvement Guide

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Package Architecture](#2-package-architecture)
3. [Core Data Model](#3-core-data-model)
4. [Design Patterns Used](#4-design-patterns-used)
5. [Complete Code Flow](#5-complete-code-flow)
   - 5.1 [Create Document](#51-create-document)
   - 5.2 [Share Document](#52-share-document)
   - 5.3 [Join Document (Presence)](#53-join-document-presence)
   - 5.4 [Edit Document (Online Path)](#54-edit-document-online-path)
   - 5.5 [Conflict Resolution via OT](#55-conflict-resolution-via-ot)
   - 5.6 [Undo / Redo](#56-undo--redo)
   - 5.7 [Version History & Restore](#57-version-history--restore)
   - 5.8 [Offline Editing & Sync](#58-offline-editing--sync)
   - 5.9 [Comments & Threads](#59-comments--threads)
   - 5.10 [Event Publishing](#510-event-publishing)
6. [Class Relationship Diagrams](#6-class-relationship-diagrams)
7. [Structural Issues & Improvements](#7-structural-issues--improvements)
8. [Interview Quick-Reference](#8-interview-quick-reference)

---

## 1. System Overview

This project is an **in-memory Low-Level Design (LLD)** of a Google Docs–style collaborative document editor in Java. It demonstrates how the major subsystems of a real collaborative editor are modelled using well-known design patterns, without any framework (no Spring, no DB, no WebSocket — everything is wired manually).

**What is covered:**

| Subsystem | Purpose |
|---|---|
| Document model | Composite structure of rich elements (Paragraph, Heading, Table, List, Image) |
| Permission model | OWNER / EDITOR / COMMENTER / VIEWER with auth checks |
| Real-time collaboration | Observer-based broadcast to connected clients |
| Operation model | Command pattern — execute, undo, redo |
| Conflict resolution | Operational Transformation (OT) strategy |
| Version history | Snapshot (Memento) pattern per edit |
| Offline editing | State pattern — Online / Offline / Syncing |
| Presence & cursor | Per-document user presence and cursor positions |
| Comment system | Threaded comments with resolve / reopen lifecycle |
| Event system | Publish-subscribe for cross-cutting concerns |
| Facade | Unified entry point (`DocumentFacade`) |

---

## 2. Package Architecture

```
com.biducollab.docs
│
├── GoogleDocsApplication.java         ← Entry point / demo runner
│
├── model/                             ← Core domain objects
│   ├── Document.java                  ← Aggregate root
│   ├── User.java
│   ├── DocumentPermission.java        ← User-Document junction
│   ├── PermissionType.java            ← Enum: OWNER, EDITOR, COMMENTER, VIEWER
│   └── element/
│       ├── DocumentElement.java       ← Interface for all block types
│       ├── ElementType.java           ← Enum
│       ├── Paragraph.java
│       ├── Heading.java
│       ├── ImageBlock.java
│       ├── Table.java / TableRow / TableCell
│       └── DocumentList.java / ListItem
│
├── repository/                        ← Data access layer (in-memory)
│   ├── DocumentRepository.java
│   ├── InMemoryDocumentRepository.java
│   ├── OperationRepository.java
│   ├── InMemoryOperationRepository.java
│   ├── VersionHistoryRepository.java
│   └── InMemoryVersionHistoryRepository.java
│
├── service/                           ← Application services
│   ├── DocumentService.java           ← CRUD operations
│   └── CollaborationService.java      ← (Thin; overlaps with DocumentCollaborationManager)
│
├── collaboration/                     ← Real-time collaboration
│   ├── DocumentObserver.java          ← Observer interface
│   ├── DocumentClient.java            ← Concrete observer
│   ├── CollaborationSession.java      ← Holds observers; broadcasts
│   ├── CollaborationSessionManager.java ← Session registry (unused in demo)
│   └── DocumentCollaborationManager.java ← Primary edit coordinator
│
├── operation/                         ← Command pattern
│   ├── EditOperation.java             ← Command interface
│   ├── InsertTextOperation.java
│   ├── DeleteTextOperation.java
│   ├── OperationLog.java              ← Per-document operation log
│   └── OperationProcessor.java        ← Process + OT transform + persist
│
├── conflict/                          ← Strategy pattern
│   ├── ConflictResolutionStrategy.java
│   └── OperationalTransformationStrategy.java ← Full 4-case OT
│
├── history/                           ← Memento pattern
│   ├── CommandHistory.java            ← Undo/Redo stacks
│   ├── DocumentSnapshot.java          ← Immutable snapshot (memento)
│   ├── DocumentContentSerializer.java ← Serializes Document → String
│   ├── VersionHistory.java            ← Wraps VersionHistoryRepository
│   └── DocumentRestoreService.java    ← Fetch snapshot by version
│
├── permission/                        ← Authorization
│   ├── AuthorizationService.java      ← canEdit / canView checks
│   ├── DocumentPermissionService.java ← CRUD permissions
│   └── DocumentSharingService.java    ← Wraps permission service
│
├── presence/                          ← Presence & cursor
│   ├── UserPresence.java
│   ├── PresenceService.java
│   ├── CursorPosition.java
│   └── CursorService.java
│
├── comment/                           ← Commenting
│   ├── Comment.java
│   └── CommentService.java
│
├── event/                             ← Event publish-subscribe
│   ├── DocumentEvent.java
│   ├── DocumentEventListener.java
│   └── DocumentEventPublisher.java    ← (Defined but not wired in demo)
│
├── facade/
│   └── DocumentFacade.java            ← Unified API (defined but not used in demo)
│
├── factory/
│   └── DocumentFactory.java           ← Creates Document with UUID
│
└── offline/                           ← State pattern for offline support
    ├── SyncState.java                 ← State interface
    ├── OnlineState.java
    ├── OfflineState.java
    ├── SyncingState.java
    ├── DocumentSyncManager.java       ← Context object
    ├── OfflineOperationQueue.java     ← Pending operations buffer
    ├── ConnectionManager.java         ← Triggers state transitions
    └── SyncService.java               ← Replays queue via OperationProcessor
```

---

## 3. Core Data Model

### Document Aggregate

```
Document
├── documentId    : String (UUID)
├── title         : String
├── ownerId       : String
├── currentVersion: int
├── elements      : List<DocumentElement>   ← Composite of block types
└── permissions   : List<DocumentPermission>
```

### Element Hierarchy (Composite Pattern)

```
DocumentElement (interface)
├── getElementId() : String
└── getType()      : ElementType

Implementations:
  Paragraph    — text: String
  Heading      — text: String
  ImageBlock   — url, altText, width, height
  Table        — List<TableRow>
    └── TableRow  — List<TableCell>
           └── TableCell — content: String
  DocumentList — List<ListItem>
    └── ListItem  — text: String
```

### Permission Model

```
DocumentPermission
├── documentId   : String
├── userId       : String
└── permissionType: PermissionType

PermissionType: OWNER > EDITOR > COMMENTER > VIEWER

canEdit  ← OWNER or EDITOR
canView  ← any permission entry present
```

### EditOperation (Command)

```
EditOperation (interface)
├── getOperationId()  : String
├── getDocumentId()   : String
├── getUserId()       : String
├── getBaseVersion()  : int
├── execute(Document)
└── undo(Document)

Implementations:
  InsertTextOperation — elementId, position, text
  DeleteTextOperation — elementId, position, length (saves deletedText for undo)
```

---

## 4. Design Patterns Used

### 4.1 Command Pattern
**Where:** `operation/` package  
**Classes:** `EditOperation`, `InsertTextOperation`, `DeleteTextOperation`, `CommandHistory`

Every user action is an object. `CommandHistory` maintains two `ArrayDeque` stacks (undo / redo).

```
User types
    │
    ▼
InsertTextOperation.execute(document)
    │
    ▼ pushed to undoStack
CommandHistory
    │
    ├── undoStack: [op3, op2, op1]
    └── redoStack: []
```

### 4.2 Observer Pattern
**Where:** `collaboration/` package  
**Classes:** `DocumentObserver`, `DocumentClient`, `CollaborationSession`

```
CollaborationSession
    │  broadcast(operation)
    ├──► DocumentClient (User A)  [skipped if sender]
    ├──► DocumentClient (User B)  ◄── receives update
    └──► DocumentClient (User C)  ◄── receives update
```

### 4.3 Strategy Pattern
**Where:** `conflict/` package  
**Classes:** `ConflictResolutionStrategy`, `OperationalTransformationStrategy`

```
OperationProcessor
    │
    └── conflictResolutionStrategy.resolve(incoming, concurrentOps)
            │
            └── OperationalTransformationStrategy
                    ├── Insert vs Insert  → shift position
                    ├── Insert vs Delete  → adjust for removed chars
                    ├── Delete vs Insert  → adjust for inserted chars
                    └── Delete vs Delete  → adjust for removed range overlap
```

### 4.4 State Pattern
**Where:** `offline/` package  
**Classes:** `SyncState`, `OnlineState`, `OfflineState`, `SyncingState`, `DocumentSyncManager`

```
DocumentSyncManager.handleEdit(doc, op)
    │
    ├── [OnlineState]   → execute immediately
    ├── [OfflineState]  → execute locally + queue for later
    └── [SyncingState]  → execute locally + queue (flush on sync)
```

### 4.5 Factory Pattern
**Where:** `factory/DocumentFactory.java`

```
DocumentFactory.createDocument(title, ownerId)
    └── new Document(UUID.randomUUID().toString(), title, ownerId)
```

### 4.6 Memento Pattern
**Where:** `history/` package  
**Classes:** `DocumentSnapshot`, `VersionHistory`, `DocumentRestoreService`

```
After each edit:
    DocumentCollaborationManager.saveSnapshot()
        │
        └── DocumentContentSerializer.serialize(document)
                │
                ▼
        DocumentSnapshot(docId, version, content, modifiedBy, now)
                │
                ▼
        VersionHistoryRepository.save(snapshot)
```

### 4.7 Composite Pattern
**Where:** `model/element/` package

```
Document.elements: List<DocumentElement>
    │
    ├── Paragraph (leaf)
    ├── Heading   (leaf)
    ├── ImageBlock (leaf)
    ├── Table     (composite)
    │     └── TableRow → TableCell
    └── DocumentList (composite)
          └── ListItem
```

### 4.8 Repository Pattern
**Where:** `repository/` package

Three repository interfaces with `InMemory*` implementations backed by `HashMap`.

### 4.9 Facade Pattern
**Where:** `facade/DocumentFacade.java`

Aggregates `DocumentService`, `DocumentSharingService`, `DocumentCollaborationManager`, `CommentService`, `PresenceService` behind a single API. *(Currently defined but not wired into the demo runner.)*

---

## 5. Complete Code Flow

### 5.1 Create Document

```
GoogleDocsApplication
    │
    ▼
DocumentFactory.createDocument("My Doc", user1.getUserId())
    │   → new Document(UUID, "My Doc", userId)
    │     constructor auto-adds OWNER permission to document.permissions
    ▼
DocumentService.createDocument(document)
    │   → documentRepository.existsById(id)  ← throws if duplicate
    │   → documentRepository.save(document)
    ▼
Document persisted in InMemoryDocumentRepository (HashMap)
```

### 5.2 Share Document

```
DocumentSharingService.shareDocument(document, targetUser, EDITOR)
    │   → println "Sharing document..."
    ▼
DocumentPermissionService.addPermission(docId, userId, EDITOR)
    │   → removes any existing permission for that user first
    │   → document.getPermissions().add(new DocumentPermission(...))
    ▼
AuthorizationService.canEdit(document, targetUser.getUserId())
    │   → checks document.getPermissions() list
    │   → returns true if OWNER or EDITOR entry exists
```

### 5.3 Join Document (Presence)

```
PresenceService.joinDocument(userId, documentId)
    │   → creates UserPresence(userId, documentId, now)
    │   → adds to Map<docId, List<UserPresence>>
    ▼
CursorService.updateCursor(userId, documentId, position=0)
    │   → creates or updates CursorPosition
    │   → stores in Map<docId, Map<userId, CursorPosition>>
    ▼
CollaborationSession.addObserver(DocumentClient)
    │   → client added to List<DocumentObserver>
```

### 5.4 Edit Document (Online Path)

This is the primary flow through `DocumentCollaborationManager`:

```
User triggers edit
    │
    ▼
DocumentCollaborationManager.handleEdit(document, operation)
    │
    ├─[1] AuthorizationService.canEdit(document, operation.getUserId())
    │         └── checks permission list → throws SecurityException if denied
    │
    ├─[2] OperationProcessor.process(document, operation)
    │         │
    │         ├─[2a] operationLog.getOperationsAfterVersion(baseVersion)
    │         │         → finds concurrent operations
    │         │
    │         ├─[2b] conflictResolutionStrategy.resolve(incoming, concurrentOps)
    │         │         → OperationalTransformationStrategy transforms positions
    │         │         → returns adjusted EditOperation
    │         │
    │         ├─[2c] transformedOp.execute(document)
    │         │         → InsertTextOperation: finds Paragraph by elementId,
    │         │           inserts text at position, calls document.incrementVersion()
    │         │
    │         └─[2d] operationLog.addOperation(transformedOp)
    │                   operationRepository.save(transformedOp)
    │
    ├─[3] saveSnapshot(document, operation.getUserId())
    │         │
    │         ├── DocumentContentSerializer.serialize(document)
    │         │         → walks elements list with instanceof checks
    │         │         → builds String representation
    │         │
    │         └── versionHistoryRepository.save(
    │                 new DocumentSnapshot(docId, version, content, userId, now))
    │
    └─[4] collaborationSession.broadcast(transformedOp)
              │
              └── for each observer (DocumentClient):
                    if client.getUserId() != operation.getUserId():
                        client.onDocumentUpdated(operation)
                        → prints received operation to console
```

### 5.5 Conflict Resolution via OT

```
Scenario: User A inserts at position 5, User B (concurrently) inserts at position 5

operationLog has: [A: Insert(pos=5, text="Hello ")]
incoming:           B: Insert(pos=5, text="World")

OperationalTransformationStrategy.resolve(B, [A]):
    │
    ├── A is InsertTextOperation, B is InsertTextOperation
    ├── A.position (5) <= B.position (5)
    │     AND (A.position < B.position OR A.operationId < B.operationId)
    │       → shift B.position by len("Hello ") = 6
    │
    └── returns B': Insert(pos=11, text="World")

Result: "Hello World" (correct convergence)

Delete vs Insert:
    If concurrent delete removed chars before incoming insert position,
    shift insert position back by deleted length.

Insert vs Delete:
    If concurrent insert is before incoming delete position,
    shift delete position forward by inserted length.

Delete vs Delete:
    Adjust incoming delete's position and length based on
    what was already removed by the concurrent delete.
```

### 5.6 Undo / Redo

```
CommandHistory.execute(operation, document)
    │   → operation.execute(document)
    │   → undoStack.push(operation)
    │   → redoStack.clear()
    ▼

CommandHistory.undo(document)
    │   → operation = undoStack.pop()
    │   → operation.undo(document)
    │       InsertTextOperation.undo: removes inserted text range
    │       DeleteTextOperation.undo: re-inserts saved deletedText at original position
    │   → redoStack.push(operation)
    ▼

CommandHistory.redo(document)
    │   → operation = redoStack.pop()
    │   → operation.execute(document)
    │   → undoStack.push(operation)
```

### 5.7 Version History & Restore

```
After each edit (via DocumentCollaborationManager.saveSnapshot):
    VersionHistoryRepository
        └── Map<docId, List<DocumentSnapshot>>
                └── snapshots grow with each edit

DocumentRestoreService.restoreVersion(documentId, version)
    │
    └── versionHistoryRepository.findByDocumentIdAndVersion(docId, version)
            └── iterates snapshot list → returns matching version
                or throws IllegalArgumentException("Version not found")
```

### 5.8 Offline Editing & Sync

```
State Transitions:
    ConnectionManager.disconnect()
        │   → DocumentSyncManager.setState(new OfflineState())
        ▼
    DocumentSyncManager.handleEdit(doc, operation)
        │   [OfflineState]:
        │   → operation.execute(document)   ← applied locally
        │   → offlineQueue.addOperation(operation)
        ▼
    ConnectionManager.connect()
        │   → DocumentSyncManager.setState(new OnlineState())
        ▼
    ConnectionManager.startSync()
        │   → DocumentSyncManager.setState(new SyncingState())
        ▼
    SyncService.sync(document, offlineQueue)
        │   → for each pendingOperation:
        │       operationProcessor.process(document, op)
        │         → OT transforms against server operations
        │         → executes + persists
        │   → offlineQueue.clear()

State-specific behaviour:
    OnlineState  → execute immediately (no queue)
    OfflineState → execute locally + queue
    SyncingState → execute locally + queue (until SyncService flushes)
```

### 5.9 Comments & Threads

```
CommentService.addComment(documentId, userId, elementId, "Great point!")
    │   → new Comment(UUID, docId, userId, elementId, text, now)
    │   → Map<docId, List<Comment>>.computeIfAbsent(...).add(comment)
    ▼
CommentService.addReply(documentId, parentCommentId, userId, "Agreed!")
    │   → finds parent comment in list
    │   → parent.addReply(new Comment(...))
    ▼
CommentService.resolveComment(documentId, commentId)
    │   → finds comment → comment.resolve()
    │     sets resolved = true
    ▼
CommentService.reopenComment(documentId, commentId)
    │   → comment.reopen() → sets resolved = false
```

### 5.10 Event Publishing

```
DocumentEventPublisher.publish(new DocumentEvent(docId, userId, "EDIT"))
    │   → for each DocumentEventListener:
    │       listener.onEvent(event)
    │
    ▼ (currently not wired into any service — improvement area)

DocumentEventPublisher can be injected into:
  - DocumentCollaborationManager (after each edit)
  - CommentService (after add/resolve)
  - DocumentSharingService (after permission change)
```

---

## 6. Class Relationship Diagrams

### Core Relationships

```
User
 │ 1
 │ * (via DocumentPermission)
 ▼
Document ──────────────────────── DocumentPermission
 │  1                                 │ permission: PermissionType
 │  *                                 │
 ▼                                    └── used by AuthorizationService
DocumentElement (interface)
 ├── Paragraph
 ├── Heading
 ├── ImageBlock
 ├── Table ──► TableRow ──► TableCell
 └── DocumentList ──► ListItem
```

### Collaboration Stack

```
DocumentCollaborationManager
 ├── AuthorizationService
 ├── OperationProcessor
 │     ├── ConflictResolutionStrategy (interface)
 │     │     └── OperationalTransformationStrategy
 │     ├── OperationLog
 │     └── OperationRepository
 ├── VersionHistoryRepository
 ├── DocumentContentSerializer
 └── CollaborationSession
       └── List<DocumentObserver>
             └── DocumentClient
```

### History / Undo

```
CommandHistory
 ├── undoStack: ArrayDeque<EditOperation>
 └── redoStack: ArrayDeque<EditOperation>

EditOperation (interface)
 ├── InsertTextOperation
 └── DeleteTextOperation
```

### Offline State Machine

```
ConnectionManager
 └── DocumentSyncManager
       ├── currentState: SyncState
       │     ├── OnlineState
       │     ├── OfflineState
       │     └── SyncingState
       └── OfflineOperationQueue
             └── List<EditOperation>

SyncService
 └── OperationProcessor (shared with online path)
```

### Facade

```
DocumentFacade (unified entry point)
 ├── DocumentService         (create / get / delete documents)
 ├── DocumentSharingService  (share / permission management)
 ├── DocumentCollaborationManager (handleEdit)
 ├── CommentService          (add / reply / resolve comments)
 └── PresenceService         (join / leave / active users)
```

---

## 7. Structural Issues & Improvements

### Issue 1 — Duplicate Edit Flow (`CollaborationService` vs `DocumentCollaborationManager`)

**Problem:** Both classes implement auth-check → execute → broadcast. `CollaborationService` also adds `CommandHistory` but uses a different `CollaborationSession` reference.

**Fix:** Remove `CollaborationService`. Route all edit flows through `DocumentCollaborationManager`. Wire `CommandHistory` into `DocumentCollaborationManager`.

```java
// Remove CollaborationService.java — its responsibilities belong here:
public class DocumentCollaborationManager {
    private final CommandHistory commandHistory;  // ADD

    public void handleEdit(Document document, EditOperation operation) {
        authorizationService.canEdit(...)
        OperationProcessor processes + OT transforms
        commandHistory.execute(operation, document);  // ADD undo support
        saveSnapshot(...)
        session.broadcast(...)
    }
}
```

---

### Issue 2 — `DocumentFacade` is Never Used

**Problem:** `GoogleDocsApplication` manually wires every service, bypassing `DocumentFacade`.

**Fix:** Refactor the demo runner to use `DocumentFacade` as the sole entry point.

```java
// GoogleDocsApplication.java — clean wiring:
DocumentFacade facade = new DocumentFacade(
    documentService, sharingService,
    collaborationManager, commentService, presenceService
);

Document doc = facade.createDocument("My Doc", user1.getUserId());
facade.joinDocument(user2.getUserId(), doc.getDocumentId());
facade.editDocument(doc, insertOp, session);
facade.addComment(doc.getDocumentId(), user2.getUserId(), elementId, "Nice!");
```

---

### Issue 3 — `DocumentEventPublisher` is Never Wired

**Problem:** The event system exists but is disconnected from all other services.

**Fix:** Inject `DocumentEventPublisher` into `DocumentCollaborationManager`, `CommentService`, and `DocumentSharingService`.

```java
// Inside DocumentCollaborationManager.handleEdit():
eventPublisher.publish(new DocumentEvent(
    document.getDocumentId(),
    operation.getUserId(),
    "DOCUMENT_EDITED"
));
```

---

### Issue 4 — `DocumentContentSerializer` Uses `instanceof` Chain

**Problem:** `DocumentContentSerializer.serialize()` uses `instanceof` checks for every element type — violates Open/Closed Principle. Adding a new element type (e.g., `VideoBlock`) requires editing this class.

**Fix:** Use the **Visitor pattern**.

```java
// Step 1 — add to DocumentElement:
interface DocumentElement {
    String getElementId();
    ElementType getType();
    String accept(DocumentElementVisitor visitor);  // ADD
}

// Step 2 — visitor interface:
interface DocumentElementVisitor {
    String visit(Paragraph paragraph);
    String visit(Heading heading);
    String visit(ImageBlock image);
    String visit(Table table);
    String visit(DocumentList list);
}

// Step 3 — implement in each element:
// Paragraph.java
public String accept(DocumentElementVisitor visitor) {
    return visitor.visit(this);
}

// Step 4 — serializer becomes a visitor:
public class DocumentContentSerializer implements DocumentElementVisitor {
    public String serialize(Document document) {
        return document.getElements().stream()
            .map(el -> el.accept(this))
            .collect(Collectors.joining("\n"));
    }
    public String visit(Paragraph p) { return p.getText(); }
    public String visit(Heading h)   { return "# " + h.getText(); }
    public String visit(ImageBlock i){ return "[IMAGE: " + i.getUrl() + "]"; }
    // ...
}
```

---

### Issue 5 — Non-Thread-Safe Collections

**Problem:** All `HashMap` / `ArrayList` usages in repositories and services are not thread-safe. Concurrent edits can corrupt state.

**Fix:** Use `ConcurrentHashMap` in repositories and `CopyOnWriteArrayList` for observer lists.

```java
// InMemoryDocumentRepository.java
private final Map<String, Document> store = new ConcurrentHashMap<>();

// CollaborationSession.java
private final List<DocumentObserver> observers = new CopyOnWriteArrayList<>();

// InMemoryVersionHistoryRepository.java
private final Map<String, List<DocumentSnapshot>> snapshotsByDocument
    = new ConcurrentHashMap<>();
```

---

### Issue 6 — `CommandHistory` is Global (Not Per-User)

**Problem:** A single shared `CommandHistory` means all users share one undo stack. User A's undo would revert User B's edit.

**Fix:** Make `CommandHistory` per-session (or per-document-per-user).

```java
// In DocumentCollaborationManager or CollaborationSession:
private final Map<String, CommandHistory> perUserHistory = new ConcurrentHashMap<>();

public void undo(Document document, String userId) {
    perUserHistory.getOrDefault(userId, new CommandHistory()).undo(document);
}
```

---

### Issue 7 — `EditOperation` Has No Server Timestamp

**Problem:** `EditOperation` only has `baseVersion` for OT ordering. True concurrency requires a server-assigned timestamp to deterministically order simultaneous operations.

**Fix:** Add `serverTimestamp` to the `EditOperation` interface.

```java
public interface EditOperation {
    // existing
    String getOperationId();
    String getDocumentId();
    String getUserId();
    int getBaseVersion();
    // add
    Instant getServerTimestamp();
    void setServerTimestamp(Instant ts);
    // existing
    void execute(Document document);
    void undo(Document document);
}
```

The `OperationProcessor` assigns the timestamp when the operation is received:

```java
operation.setServerTimestamp(Instant.now());
```

---

### Issue 8 — Inconsistent Use of Lombok

**Problem:** Only `User.java` uses `@AllArgsConstructor`. All other classes have manual constructors.

**Fix:** Either apply Lombok consistently across all model classes, or remove it from `User.java` and use a standard constructor everywhere.

```java
// Option A — extend Lombok to all model POJOs:
@Data
@AllArgsConstructor
public class DocumentPermission { ... }

// Option B — remove from User.java, use plain constructor:
public User(String userId, String name, String email) {
    this.userId = userId; this.name = name; this.email = email;
}
```

---

### Issue 9 — Hinglish / Informal Comments

**Problem:** Several files contain comments in Hindi-English mix (e.g., *"Ye class document ka basic lifecycle manage karegi"*). This is not professional for a production or interview codebase.

**Fix:** Replace all informal comments with concise, professional English.

```java
// Before:
// Ye class document ka basic lifecycle manage karegi

// After:
// Manages the document lifecycle: creation, retrieval, and deletion.
```

---

### Issue 10 — `VersionHistory` is a Trivial Wrapper

**Problem:** `VersionHistory` wraps `VersionHistoryRepository` with no additional logic, adding an unnecessary layer.

**Fix:** Remove `VersionHistory` as a class. Inject `VersionHistoryRepository` directly into `DocumentCollaborationManager` and `DocumentRestoreService`.

---

### Issue 11 — `DocumentSharingService` Thin Layer

**Problem:** `DocumentSharingService` wraps `DocumentPermissionService` and only adds `System.out.println`. It provides no real abstraction.

**Fix:** Either add meaningful logic (event publishing, audit logging) or fold it into `DocumentPermissionService` and remove the wrapper.

```java
// Enhanced DocumentSharingService (meaningful layer):
public void shareDocument(Document document, User targetUser, PermissionType type) {
    documentPermissionService.addPermission(
        document.getDocumentId(), targetUser.getUserId(), type);
    eventPublisher.publish(new DocumentEvent(
        document.getDocumentId(), targetUser.getUserId(), "DOCUMENT_SHARED"));
    notificationService.notify(targetUser,
        "You have been granted " + type + " access.");
}
```

---

### Summary Table

`✅ Applied` = fixed in code. `⚠️ Pending` = design recommendation only.

| # | Issue | Severity | Status | Fix |
|---|---|---|---|---|
| 1 | Duplicate edit flow (`CollaborationService` + `DCM`) | High | ✅ Applied | `CollaborationService` marked `@Deprecated`; `DocumentCollaborationManager` is the sole edit coordinator |
| 2 | `DocumentFacade` unused | High | ⚠️ Pending | Wire it in `GoogleDocsApplication` as the entry point |
| 3 | `DocumentEventPublisher` disconnected | Medium | ⚠️ Pending | Inject into `DCM`, `CommentService`, `SharingService` |
| 4 | `instanceof` in serializer | Medium | ✅ Applied | `DocumentContentSerializer` now implements `DocumentElementVisitor<String>`; all elements have `accept()` |
| 5 | Non-thread-safe collections | High | ✅ Applied | `ConcurrentHashMap` in all repositories; `CopyOnWriteArrayList` in `CollaborationSession` and `DocumentEventPublisher` |
| 6 | Global `CommandHistory` | Medium | ✅ Applied | Per-user `ConcurrentHashMap<String, CommandHistory>` in `DocumentCollaborationManager`; `undo(userId, doc)` / `redo(userId, doc)` methods added |
| 7 | No server timestamp on operation | Medium | ✅ Applied | `getServerTimestamp()` / `setServerTimestamp()` added to `EditOperation`; `OperationProcessor` stamps on arrival |
| 8 | Inconsistent Lombok | Low | ⚠️ Pending | Standardize (all model classes or none) |
| 9 | Hinglish comments | Low | ✅ Applied | All Hinglish/informal comments replaced with professional English Javadoc |
| 10 | `VersionHistory` trivial wrapper | Low | ⚠️ Pending | Remove and inject `VersionHistoryRepository` directly |
| 11 | `DocumentSharingService` thin layer | Low | ⚠️ Pending | Add event publishing + audit logging or consolidate into `DocumentPermissionService` |

---

## 8. Interview Quick-Reference

### Why Command Pattern for edits?
Every user action becomes an object. Enables undo/redo, operation log, OT transforms, broadcast, and replay from history without coupling the UI to document mutation logic.

### How does OT work?
Each client sends an operation with a `baseVersion`. The server collects operations concurrent to that version, transforms the incoming operation's position/length against each concurrent operation, then applies and broadcasts the adjusted operation. All clients converge to the same state.

### Why Observer for broadcast?
A single accepted edit must be pushed to N connected clients. Observer decouples the edit pipeline from the delivery mechanism — new clients can subscribe/unsubscribe without touching the core edit logic.

### How does offline sync work?
- `SyncState` interface with three concrete states (`OnlineState`, `OfflineState`, `SyncingState`)
- `OfflineState`: execute locally + queue to `OfflineOperationQueue`
- On reconnect: `SyncService` replays the queue through `OperationProcessor`, which OT-transforms each pending op against server ops that arrived during the outage

### Why Visitor over `instanceof` in serializer?
`instanceof` chains break the Open/Closed Principle — adding a new element type requires editing `DocumentContentSerializer`. With Visitor, each element type provides its own `accept()`, and new serializers can be added without touching element code.

### How to scale this system?
- Route document sessions to dedicated servers using sticky routing
- Pub/Sub (Kafka / Redis) for cross-server broadcast
- Persistent operation log in a write-optimized store (e.g., Cassandra)
- Periodic snapshot to avoid full log replay
- Presence data in Redis (ephemeral, TTL-based)
