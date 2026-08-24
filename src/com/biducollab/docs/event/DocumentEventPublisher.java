package com.biducollab.docs.event;

import java.util.ArrayList;
import java.util.List;

// Publishes document events to listeners
public class DocumentEventPublisher {

    private final List<DocumentEventListener> listeners =
            new ArrayList<>();

    // Register an event listener
    public void registerListener(
            DocumentEventListener listener) {

        listeners.add(listener);
    }

    // Remove an event listener
    public void removeListener(
            DocumentEventListener listener) {

        listeners.remove(listener);
    }

    // Notify all listeners about an event
    public void publish(
            DocumentEvent event) {

        for (DocumentEventListener listener : listeners) {

            listener.onEvent(event);
        }
    }
}
/*
User edits document
        |
        v
DocumentEvent
        |
        v
DocumentEventPublisher
        |
        +------------------+
        |                  |
        v                  v
NotificationListener   AnalyticsListener
 */