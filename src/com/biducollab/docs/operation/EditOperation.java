package com.biducollab.docs.operation;

import com.biducollab.docs.model.Document;

public interface EditOperation {

    String getOperationId();

    String getDocumentId();

    String getUserId();

    int getBaseVersion();

    void execute(Document document);

    void undo(Document document);
}

/*
EditOperation
│
├── operationId
│      → Har edit ki unique identity
│
├── documentId
│      → Kis document par edit ho raha hai
│
├── userId
│      → Kis user ne edit kiya
│
├── baseVersion
│      → User ne document ka kaunsa version dekhkar edit kiya
│
├── execute()
│      → Operation apply karega
│
└── undo()
       → Operation reverse karega
 */