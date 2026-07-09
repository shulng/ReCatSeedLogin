package cc.baka9.catseedlogin.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple event bus for CatSeedLogin events.
 * Thread-safe, supports listener registration/unregistration.
 */
public final class CatSeedEventBus {

    private final Map<Class<? extends CatSeedEvent>, List<CatSeedListener<?>>> listeners = new ConcurrentHashMap<>();

    public CatSeedEventBus() {}

    /**
     * Register a listener for a specific event type.
     */
    @SuppressWarnings("unchecked")
    public <T extends CatSeedEvent> void register(Class<T> type, CatSeedListener<T> listener) {
        listeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * Unregister a listener for a specific event type.
     */
    @SuppressWarnings("unchecked")
    public <T extends CatSeedEvent> void unregister(Class<T> type, CatSeedListener<T> listener) {
        List<CatSeedListener<?>> list = listeners.get(type);
        if (list != null) {
            list.remove(listener);
        }
    }

    /**
     * Fire an event to all registered listeners.
     * Listeners are called in registration order.
     */
    @SuppressWarnings("unchecked")
    public <T extends CatSeedEvent> void fire(T event) {
        List<CatSeedListener<?>> list = listeners.get(event.getEventType());
        if (list == null) return;
        for (CatSeedListener<?> listener : list) {
            ((CatSeedListener<T>) listener).onEvent(event);
            if (event.isCancelled()) break;
        }
    }
}
