package com.biducollab.docs.repository;

import com.biducollab.docs.version.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// Stores document version history in memory; thread-safe via ConcurrentHashMap.
public class InMemoryVersionHistoryRepository
        implements VersionHistoryRepository {

    private final Map<String, List<DocumentSnapshot>>
            snapshotsByDocument = new ConcurrentHashMap<>();

    @Override
    public void save(DocumentSnapshot snapshot) {

        snapshotsByDocument
                .computeIfAbsent(
                        snapshot.getDocumentId(),
                        key -> new CopyOnWriteArrayList<>()
                )
                .add(snapshot);
    }

    @Override
    public List<DocumentSnapshot> findByDocumentId(
            String documentId) {

        return new ArrayList<>(
                snapshotsByDocument.getOrDefault(
                        documentId,
                        new ArrayList<>()
                )
        );
    }

    @Override
    public DocumentSnapshot findByDocumentIdAndVersion(
            String documentId,
            int version) {

        List<DocumentSnapshot> snapshots =
                snapshotsByDocument.get(documentId);

        if (snapshots == null) {
            return null;
        }

        for (DocumentSnapshot snapshot : snapshots) {

            if (snapshot.getVersion() == version) {
                return snapshot;
            }
        }

        return null;
    }

    @Override
    public DocumentSnapshot findLatest(
            String documentId) {

        List<DocumentSnapshot> snapshots =
                snapshotsByDocument.get(documentId);

        if (snapshots == null || snapshots.isEmpty()) {
            return null;
        }

        return snapshots.get(
                snapshots.size() - 1
        );
    }
}