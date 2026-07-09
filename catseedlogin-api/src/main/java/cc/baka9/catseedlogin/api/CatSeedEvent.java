package cc.baka9.catseedlogin.api;

/**
 * Base class for all CatSeedLogin events.
 * Events can be cancelled to prevent the default action.
 */
public abstract class CatSeedEvent {

    private boolean cancelled;

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Get the event type for listener registration.
     */
    @SuppressWarnings("unchecked")
    public Class<? extends CatSeedEvent> getEventType() {
        return getClass();
    }
}
