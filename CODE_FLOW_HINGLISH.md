# Google Docs LLD — Code Flow (Hinglish Edition)

> Yeh document ek casual, step-by-step walkthrough hai poore codebase ka.  
> Har feature ka flow clearly explain kiya gaya hai — kahan se shuru hota hai,  
> kahan jaata hai, aur kya hota hai beech mein.

---

## Pehle bata dete hain — yeh project hai kya?

Ek **in-memory Google Docs clone** — no Spring, no database, no framework.  
Sab kuch manually wired hai. Basically ek LLD (Low Level Design) exercise hai
jisme real Google Docs ki features simulate ki gayi hain:

| Feature | Kya karta hai |
|---|---|
| Document create karna | Factory pattern se UUID wala doc banana |
| Real-time editing | OT (Operational Transformation) se conflicts handle karna |
| Undo / Redo | Har user ka apna stack |
| Version History | Memento pattern — har edit pe snapshot lena |
| Collaboration | Observer pattern — doosre users ko broadcast karna |
| Permissions | Share karna VIEWER/EDITOR/OWNER rights se |
| Comments | Kisi bhi element pe comment lagana |
| Presence | Kaun online hai, cursor kahan hai |
| Offline Sync | State pattern — offline/online/syncing states |
| Facade | Ek single clean API baaki sab ke upar |

---

## Entry Point — `GoogleDocsApplication.java`

Yahan se sab shuru hota hai. Sab classes manually create hoti hain,
koi dependency injection framework nahi hai.

```
main()
  |
  ├── Repositories banao      (InMemoryDocumentRepository, InMemoryOperationRepository, InMemoryVersionHistoryRepository)
  ├── Services banao          (DocumentService, AuthorizationService, OperationLog, VersionHistory)
  ├── Collaboration banao     (CollaborationSession, OperationProcessor, DocumentCollaborationManager)
  ├── Presence banao          (PresenceService, CursorService)
  ├── Document create karo    (DocumentFactory → DocumentService.createDocument)
  ├── Users add karo session mein (CollaborationSession.addObserver)
  └── Comment lagao           (CommentService.addComment)
```

**Ek important baat:** `DocumentFacade` ek clean wrapper hai in sab services ka.
Ideally `main()` sirf `DocumentFacade` se baat kare, directly services se nahi.

---

## Feature 1 — Document Banana (Factory Pattern)

**Classes:** `DocumentFactory` → `Document` → `DocumentService` → `DocumentRepository`

```
DocumentFactory.createDocument("My Doc", "user-1")
        |
        | → new Document(UUID.randomUUID(), title, ownerId)
        | → Document constructor ke andar:
        |       permissions.add(new DocumentPermission(docId, ownerId, PermissionType.OWNER))
        |       currentVersion = 0
        |
        v
DocumentService.createDocument(document)
        |
        v
InMemoryDocumentRepository.save(document)
        |
        v
ConcurrentHashMap<documentId, Document> mein store ho gaya ✅
```

**Pattern:** `DocumentFactory` — creation logic alag rakhta hai `Document` se.
Factory ke through banana mandatory nahi, but recommended hai taaki
UUID generation, default permissions sab ek jagah rahein.

---

## Feature 2 — Users Ko Document Share Karna (Permission System)

**Classes:** `DocumentSharingService` → `DocumentPermissionService` → `Document`

```
sharingService.shareDocument(document, "user-2", PermissionType.EDITOR)
        |
        v
DocumentPermissionService.addPermission(document, "user-2", EDITOR)
        |
        v
document.getPermissions().add(
    new DocumentPermission(docId, "user-2", PermissionType.EDITOR)
)
```

**Permission types:** `OWNER > EDITOR > VIEWER`

Jab bhi koi edit karne aayega, pehle `AuthorizationService.canEdit()` check karega:

```
AuthorizationService.canEdit(document, userId)
        |
        v
document.getPermissions() mein dhundho userId ka permission
        |
        ├── OWNER  → true ✅
        ├── EDITOR → true ✅
        └── VIEWER → false ❌ (SecurityException throw hogi)
```

**Note:** `DocumentPermission` aur `PermissionType` `model/` package mein hain
kyunki `Document` ka constructor inhe directly use karta hai.
Agar `permission/` package mein move karte toh circular dependency ho jaati
(`model` ↔ `permission`). Isliye wahan rakhna theek hai.

---

## Feature 3 — Real-Time Editing (The Main Flow 🔥)

Yeh sabse important flow hai. Jab user kuch type karta hai, yeh poora pipeline
chalti hai:

### Step 1 — `DocumentFacade.editDocument()`

```
User types "Hello" at position 5
        |
        v
DocumentFacade.editDocument(document, insertOperation)
        |
        v
DocumentCollaborationManager.handleEdit(document, operation)
```

### Step 2 — Permission Check

```
DocumentCollaborationManager.handleEdit()
        |
        v
authorizationService.canEdit(document, operation.getUserId())
        |
        ├── Nahi hai permission → SecurityException throw karo, ruk jao
        └── Hai permission → aage badho ✅
```

### Step 3 — OT Pipeline (`OperationProcessor`)

```
operationProcessor.process(document, incomingOperation)
        |
        v
STEP A: Server timestamp assign karo
        incomingOperation.setServerTimestamp(Instant.now())
        (yeh timestamp OT mein tie-breaker ka kaam karta hai)
        |
        v
STEP B: Concurrent operations dhundo
        operationLog.getOperationsAfterVersion(
            docId,
            incomingOperation.getBaseVersion()
        )
        → Matlab: mere baseVersion ke baad jo bhi operations apply
          ho chuke hain unhe dhundo
        |
        v
STEP C: Conflict resolve karo
        conflictResolutionStrategy.resolve(incoming, concurrentOps)
        → OperationalTransformationStrategy.resolve() call hogi
        |
        v
STEP D: Transformed operation document pe apply karo
        transformedOperation.execute(document)
        |
        v
STEP E: Persist karo log mein
        operationLog.addOperation(transformedOperation)
```

### Step 4 — Undo Stack Mein Record Karo

```
commandHistoryFor(userId).record(processedOperation)
        |
        v
Per-user CommandHistory mein undoStack.push(operation)
redoStack.clear() ← naya edit kiya toh redo history gayab
```

### Step 5 — Version History Snapshot

```
saveSnapshot(document, userId)
        |
        v
documentContentSerializer.serialize(document)
        → Visitor pattern se sab elements ko text mein convert karo
        → Paragraph → text content
        → Heading → "# heading text"
        → Table → rows ka content
        → ImageBlock → "[Image: url]"
        |
        v
new DocumentSnapshot(docId, currentVersion, content, userId)
        |
        v
versionHistory.addSnapshot(snapshot)
        |
        v
InMemoryVersionHistoryRepository.save(snapshot)
        → snapshotsByDocument.computeIfAbsent(docId, ...).add(snapshot)
```

### Step 6 — Baaki Users Ko Broadcast Karo (Observer Pattern)

```
collaborationSession.broadcast(processedOperation)
        |
        v
observers list mein jaao (DocumentClient objects)
        |
        v
Har observer ke liye:
    observer.onDocumentUpdated(processedOperation)
        |
        v
DocumentClient.onDocumentUpdated()
    → "User user-1 received update: <operationId>" print hoga
```

### Poora Flow Ek Jagah:

```
User edits
    ↓
DocumentFacade.editDocument()
    ↓
DocumentCollaborationManager.handleEdit()
    ↓
[1] AuthorizationService.canEdit()    ← Permission check
    ↓
[2] OperationProcessor.process()      ← OT Pipeline
    ├── setServerTimestamp()
    ├── getOperationsAfterVersion()
    ├── OTStrategy.resolve()
    ├── operation.execute(document)
    └── operationLog.addOperation()
    ↓
[3] CommandHistory.record()           ← Per-user undo stack
    ↓
[4] saveSnapshot()                    ← Version history
    ↓
[5] CollaborationSession.broadcast()  ← Observer notify
    └── DocumentClient.onDocumentUpdated() (har connected user ke liye)
```

---

## Feature 4 — Operational Transformation (OT) — Conflict Kaise Solve Hota Hai?

**Scenario:** "HelloWorld" document hai. Dono users same version pe hain.

- **User A:** Position 5 pe `" "` insert karo → "Hello World"
- **User B:** Position 5 pe `"Beautiful "` insert karo → "HelloBeautiful World"

Problem: User B ka operation ab **wrong position** pe apply hoga
kyunki User A ka insert already ho chuka hai.

**Solution — `OperationalTransformationStrategy`:**

```
User B ka incoming operation → position: 5, text: "Beautiful "
        |
        v
concurrent operations mein User A ka insert hai: position: 5, text: " "
        |
        v
transformInsertAgainstInsert()
        |
        v
concurrent.position (5) == incoming.position (5)
        |
        v
Tie-break: operationId compare karo (lexicographic)
User A ka ID < User B ka ID → User A pehle apply hua
        |
        v
newPosition = 5 + " ".length() = 6
        |
        v
Transformed operation: position: 6, text: "Beautiful "
        |
        v
Apply: "Hello " + "Beautiful " + "World" = "Hello Beautiful World" ✅
```

**Chaar cases handle kiye gaye hain:**

| Incoming | Concurrent | Kya hota hai |
|---|---|---|
| Insert | Insert | Agar concurrent pehle hai → position shift karo |
| Insert | Delete | Agar delete pehle tha → position kam karo; agar delete ke andar tha → clamp karo |
| Delete | Insert | Agar insert pehle tha → delete position aage badho |
| Delete | Delete | Agar concurrent pehle tha → position kam karo; overlap ho to clamp karo |

---

## Feature 5 — Undo / Redo (Command Pattern)

Har user ka **apna independent** `CommandHistory` hai.
User A ka undo sirf User A ke edits revert karega, User B ke nahi.

```java
// Per-user map in DocumentCollaborationManager
Map<String, CommandHistory> userCommandHistories = new ConcurrentHashMap<>();
```

### Undo Flow:

```
user calls undo("user-1", document)
        |
        v
commandHistoryFor("user-1")        ← user-1 ka specific stack
        |
        v
CommandHistory.undo(document)
        |
        v
undoStack.pop() → last operation nikalo
        |
        v
operation.undo(document)           ← InsertTextOperation → delete karo
                                      DeleteTextOperation → insert karo
        |
        v
redoStack.push(operation)          ← redo ke liye save karo
```

### Redo Flow:

```
user calls redo("user-1", document)
        |
        v
CommandHistory.redo(document)
        |
        v
redoStack.pop() → last undone operation nikalo
        |
        v
operation.execute(document)        ← wapas apply karo
        |
        v
undoStack.push(operation)          ← undo ke liye wapas push karo
```

### `execute()` vs `record()` — dono kab use hote hain?

```
execute() → jab CommandHistory khud execute bhi kare + record bhi kare
            (standalone use case ke liye)

record()  → jab operation already execute ho chuka ho (OT pipeline mein)
            CommandHistory sirf stack mein push karta hai, dobara execute nahi karta
            (DocumentCollaborationManager.handleEdit() yahi use karta hai)
```

---

## Feature 6 — Version History (Memento Pattern)

Har successful edit ke baad **ek snapshot** save hota hai.

**Classes:** `DocumentSnapshot` (Memento) → `VersionHistory` → `InMemoryVersionHistoryRepository`

```
DocumentSnapshot {
    documentId: "doc-abc123"
    version: 5
    content: "Hello Beautiful World..."   ← serialized text
    modifiedBy: "user-1"
    modifiedAt: 2026-08-24T10:30:00
}
```

### Version restore karna:

```
DocumentRestoreService.restoreVersion(documentId, version)
        |
        v
versionHistory.getVersion(documentId, version)
        |
        v
Snapshot mila → document.setContent(snapshot.getContent())
Snapshot nahi mila → IllegalArgumentException throw karo
```

### History dekhna:

```
versionHistory.getHistory(documentId)
        |
        v
InMemoryVersionHistoryRepository.findByDocumentId(documentId)
        |
        v
CopyOnWriteArrayList<DocumentSnapshot> return karo
```

**`CopyOnWriteArrayList` kyun?**  
Multiple users concurrently snapshot add kar sakte hain.
`CopyOnWriteArrayList` write pe copy banata hai, reads ko block nahi karta.

---

## Feature 7 — Presence & Cursor (Kaun Online Hai, Cursor Kahan Hai)

**Classes:** `PresenceService`, `CursorService`, `UserPresence`, `CursorPosition`

### User join karna:

```
presenceService.joinDocument("doc-abc", "user-1")
        |
        v
activeUsers.computeIfAbsent("doc-abc", ...).add("user-1")
        |
        v
"user-1 joined the document" print hoga
```

### Cursor update karna:

```
cursorService.updateCursor("doc-abc", "user-1", position: 10)
        |
        v
new CursorPosition("user-1", 10, now())
        |
        v
cursorPositions["doc-abc"]["user-1"] = new position
```

### User leave karna:

```
presenceService.leaveDocument("doc-abc", "user-2")
        |
        v
activeUsers["doc-abc"].remove("user-2")
        |
        v
"user-2 left the document" print hoga
```

---

## Feature 8 — Comments

**Classes:** `Comment`, `CommentService`

```
new Comment("comment-1", docId, "user-1", "element-1", "Please review this")
        |
        v
commentService.addComment(comment)
        |
        v
comments list mein add karo
```

Comment ek specific **element** pe lagta hai (`elementId` — paragraph, heading, etc.).
`getComments(documentId)` se document ke sab comments mil jaate hain.

---

## Feature 9 — Offline Sync (State Pattern)

Jab network chali jaaye toh kya hoga? Yahan **State Pattern** use hua hai.

**States:** `OnlineState`, `OfflineState`, `SyncingState`  
**Context:** `DocumentSyncManager`  
**Controller:** `ConnectionManager`

### States ka behaviour:

```
OnlineState.handleEdit(document, operation, queue)
    → operation.execute(document)   ← seedha apply karo
    → print "Online: operation applied"

OfflineState.handleEdit(document, operation, queue)
    → operation.execute(document)   ← locally apply karo (optimistic)
    → queue.addOperation(operation) ← queue mein bhi daalo sync ke liye

SyncingState.handleEdit(document, operation, queue)
    → operation.execute(document)   ← locally apply karo
    → queue.addOperation(operation) ← queue mein daalo (sync chal rahi hai abhi)
```

### Network events:

```
connectionManager.disconnect()
    → connected = false
    → syncManager.setState(new OfflineState())
    → "Connection lost" print hoga

connectionManager.connect()
    → connected = true
    → syncManager.setState(new OnlineState())
    → "Connection restored" print hoga

connectionManager.startSync()
    → syncManager.setState(new SyncingState())
    → (SyncService alag se queue flush karega)
```

### Sync pipeline:

```
connectionManager.connect()
        |
        v
SyncService.sync(document)
        |
        v
offlineQueue.getOperations() → pending operations nikalo
        |
        v
Har operation ke liye:
    OperationProcessor.process(document, op)   ← OT ke through send karo
    queue.removeOperation(op)                  ← queue se hata do
        |
        v
"Sync complete. X operations synced." print hoga
```

---

## Feature 10 — Document Serialization (Visitor Pattern)

Jab version snapshot banani ho ya content export karna ho, document ke
sab elements ko text mein convert karna padta hai. Yahan **Visitor Pattern** use hua.

**Classes:** `DocumentElementVisitor<T>` (interface), `DocumentContentSerializer` (visitor)

```
documentContentSerializer.serialize(document)
        |
        v
Har element ke liye element.accept(this) call hoga
        |
        ├── Paragraph  → paragraph.accept(visitor)
        |       → visitor.visitParagraph(this) → text content return karo
        |
        ├── Heading    → heading.accept(visitor)
        |       → visitor.visitHeading(this) → "# " + text return karo
        |
        ├── Table      → table.accept(visitor)
        |       → visitor.visitTable(this) → rows ka content return karo
        |
        ├── ImageBlock → image.accept(visitor)
        |       → visitor.visitImage(this) → "[Image: url]" return karo
        |
        └── DocumentList → list.accept(visitor)
                → visitor.visitList(this) → "• item1\n• item2" return karo
        |
        v
Sab strings join karo → final serialized content ✅
```

**Visitor pattern ka fayda:** Agar kal naya element type aaya (e.g., `CodeBlock`),
sirf `DocumentElementVisitor` interface mein ek method add karo aur
`DocumentContentSerializer` mein implement karo.
Existing elements ko touch nahi karna padega.

---

## Design Patterns — Quick Reference

| Pattern | Class(es) | Kahan use hua |
|---|---|---|
| **Factory** | `DocumentFactory` | Document creation with UUID + defaults |
| **Command** | `EditOperation`, `CommandHistory` | Undo/Redo per user |
| **Observer** | `DocumentObserver`, `DocumentClient`, `CollaborationSession` | Real-time broadcast |
| **Strategy** | `ConflictResolutionStrategy`, `OperationalTransformationStrategy` | OT algorithm swap karna |
| **State** | `SyncState`, `OnlineState`, `OfflineState`, `SyncingState` | Network state transitions |
| **Memento** | `DocumentSnapshot`, `VersionHistory` | Version history save karna |
| **Visitor** | `DocumentElementVisitor`, `DocumentContentSerializer` | Document content serialize karna |
| **Facade** | `DocumentFacade` | Sab services ka single clean API |
| **Repository** | `DocumentRepository`, `VersionHistoryRepository`, etc. | Storage abstraction |

---

## Package Structure — Ek Nazar Mein

```
com.biducollab.docs
│
├── collaboration/    ← Real-time editing ka core
│   ├── CollaborationSession        (Observer — broadcast karta hai)
│   ├── CollaborationSessionManager (Multiple sessions manage karta hai)
│   ├── DocumentClient              (Observer — updates receive karta hai)
│   ├── DocumentCollaborationManager (Central coordinator — SAB kuch yahan aata hai)
│   └── DocumentObserver            (Interface — observer contract)
│
├── comment/          ← Comments feature
│
├── document/         ← Document lifecycle
│   ├── DocumentContentSerializer   (Visitor — serialization)
│   ├── DocumentFactory             (Factory — creation)
│   └── DocumentService             (CRUD operations)
│
├── event/            ← Event system (extensibility ke liye)
│   ├── DocumentEvent
│   ├── DocumentEventListener
│   └── DocumentEventPublisher
│
├── facade/           ← Single entry point
│   └── DocumentFacade
│
├── model/            ← Core domain objects
│   ├── Document, DocumentPermission, PermissionType, User
│   └── element/      ← Document building blocks
│       ├── DocumentElement (interface)
│       ├── DocumentElementVisitor (interface)
│       ├── Paragraph, Heading, Table, ImageBlock, DocumentList
│       └── TableRow, TableCell, ListItem, ElementType
│
├── offline/          ← Offline support
│   ├── ConnectionManager, DocumentSyncManager
│   ├── OfflineOperationQueue, SyncService
│   └── state/        ← State pattern
│       ├── SyncState (interface)
│       ├── OnlineState, OfflineState, SyncingState
│
├── operation/        ← Edit operations + undo/redo
│   ├── EditOperation (interface — Command pattern)
│   ├── InsertTextOperation, DeleteTextOperation
│   ├── CommandHistory (per-user undo/redo)
│   ├── OperationLog (persistence)
│   ├── OperationProcessor (OT pipeline coordinator)
│   └── conflict/
│       ├── ConflictResolutionStrategy (interface)
│       └── OperationalTransformationStrategy (implementation)
│
├── permission/       ← Access control
│   ├── AuthorizationService        (canEdit, canView check)
│   ├── DocumentPermissionService   (permissions add/remove)
│   └── DocumentSharingService      (share workflow)
│
├── presence/         ← Online users + cursors
│   ├── PresenceService, UserPresence
│   └── CursorService, CursorPosition
│
├── repository/       ← Storage layer
│   ├── Interfaces: DocumentRepository, OperationRepository, VersionHistoryRepository
│   └── Implementations: InMemory* (ConcurrentHashMap based)
│
├── version/          ← Version history
│   ├── DocumentSnapshot    (Memento)
│   ├── VersionHistory      (save/fetch snapshots)
│   └── DocumentRestoreService (restore to previous version)
│
└── GoogleDocsApplication.java  ← Entry point
```

---

## Thread Safety — Kahan Kahan Dhyan Rakha Gaya Hai?

Ye ek multi-user system hai — concurrent access common hai:

| Class | Thread-safe mechanism |
|---|---|
| `InMemoryDocumentRepository` | `ConcurrentHashMap` |
| `InMemoryOperationRepository` | `ConcurrentHashMap` + `CopyOnWriteArrayList` |
| `InMemoryVersionHistoryRepository` | `ConcurrentHashMap` + `CopyOnWriteArrayList` |
| `DocumentCollaborationManager` | `ConcurrentHashMap` (userCommandHistories) |
| `PresenceService` | `ConcurrentHashMap` + `CopyOnWriteArrayList` |
| `CursorService` | `ConcurrentHashMap` |
| `OfflineOperationQueue` | `CopyOnWriteArrayList` |

`CopyOnWriteArrayList` kyun? Read operations bahut zyada hain, writes kam.
Read pe lock nahi lagta — performance better hai.

---

## End-to-End Example — User A "Hello " likhta hai, User B "World" likhta hai

```
Initial document: ""   (version 0)

--- User A ---
InsertTextOperation: pos=0, text="Hello ", baseVersion=0
    ↓ DocumentCollaborationManager.handleEdit()
    ↓ AuthorizationService.canEdit() → ✅ User A is EDITOR
    ↓ OperationProcessor.process()
        → setServerTimestamp(T1)
        → concurrentOps = [] (koi concurrent nahi)
        → OTStrategy.resolve() → no transform needed
        → execute: document content = "Hello "
        → operationLog.add(op_A)
    ↓ commandHistoryFor("user-A").record(op_A)
    ↓ saveSnapshot: version 1, content="Hello "
    ↓ collaborationSession.broadcast(op_A)
        → user-B.onDocumentUpdated(op_A) ← User B ko notify kiya

--- User B (same baseVersion=0, concurrent hai!) ---
InsertTextOperation: pos=0, text="World", baseVersion=0
    ↓ DocumentCollaborationManager.handleEdit()
    ↓ AuthorizationService.canEdit() → ✅ User B is EDITOR
    ↓ OperationProcessor.process()
        → setServerTimestamp(T2)
        → concurrentOps = [op_A] ← User A ka operation already apply hua
        → OTStrategy.resolve(op_B, [op_A])
            → transformInsertAgainstInsert()
            → op_A.position (0) == op_B.position (0)
            → Tie-break: op_A.id < op_B.id → op_A pehle → shift op_B
            → newPosition = 0 + "Hello ".length() = 6
        → transformed op_B: pos=6, text="World"
        → execute: document content = "Hello World"
        → operationLog.add(transformed_op_B)
    ↓ commandHistoryFor("user-B").record(transformed_op_B)
    ↓ saveSnapshot: version 2, content="Hello World"
    ↓ collaborationSession.broadcast(transformed_op_B)
        → user-A.onDocumentUpdated(transformed_op_B) ← User A ko notify kiya

Final document: "Hello World" ✅
Dono users ke paas same document hai ✅
```

---

*Agar koi cheez unclear lage ya aur detail chahiye kisi specific feature mein,
bas pooch lo!*
