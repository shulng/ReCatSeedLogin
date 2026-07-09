package cc.baka9.catseedlogin.api;

/**
 * Functional interface for CatSeedLogin event listeners.
 */
@FunctionalInterface
public interface CatSeedListener<T extends CatSeedEvent> {
    void onEvent(T event);
}
