package cc.baka9.catseedlogin.api;

/**
 * Registry for plugin hooks and extensions.
 * Allows third-party plugins to customize authentication behavior.
 */
public interface HookRegistry {

    /**
     * Register a pre-login hook for custom validation.
     */
    void registerPreLoginHook(PreLoginHook hook);

    /**
     * Register a custom password policy.
     * Replaces the default password validation.
     */
    void registerPasswordPolicy(PasswordPolicy policy);

    /**
     * Set a custom session manager.
     * Allows other plugins to manage login sessions.
     */
    void setSessionManager(SessionManager sessionManager);

    /**
     * Set a custom account storage backend.
     * Allows other plugins to provide database implementations.
     */
    void setAccountStorage(AccountStorage storage);

    /**
     * Get the registered password policy.
     * Returns the default if no custom policy is registered.
     */
    PasswordPolicy getPasswordPolicy();

    /**
     * Get the registered session manager.
     */
    SessionManager getSessionManager();

    /**
     * Get the registered account storage.
     */
    AccountStorage getAccountStorage();
}
