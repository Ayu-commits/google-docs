package com.biducollab.docs.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Publishes {@link DocumentEvent}s to all registered {@link DocumentEventListener}s.
 *
 * <p>{@code CopyOnWriteArrayList} is used so that listeners can be added or removed
 * from one thread while {@link #publish} iterates the list on another thread without
 * throwing {@code ConcurrentModificationException}.
 *
 * <p>Example consumers: notification service, analytics tracker, audit logger.
 */
public class DocumentEventPublisher {

    private final List<DocumentEventListener> listeners =
            new CopyOnWriteArrayList<>();

    /** Register a listener to receive future events. */
    public void registerListener(DocumentEventListener listener) {
        listeners.add(listener);
    }

    /** Deregister a previously registered listener. */
    public void removeListener(DocumentEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Deliver the event synchronously to every registered listener.
     * Listeners are notified in registration order.
     */
    public void publish(DocumentEvent event) {

        for (DocumentEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
/*
Flow

DocumentCollaborationManager (or any service)
        |
        v
DocumentEventPublisher.publish(event)
        |
        +------------------+------------------+
        |                  |                  |
        v                  v                  v
NotificationListener  AnalyticsListener  AuditListener
 */
