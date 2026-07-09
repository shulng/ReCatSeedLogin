package cc.baka9.catseedlogin.common.hook;

import cc.baka9.catseedlogin.api.*;

/**
 * Default implementation of HookRegistry.
 */
public class HookRegistryImpl implements HookRegistry {

    private PreLoginHook preLoginHook;
    private PasswordPolicy passwordPolicy;
    private SessionManager sessionManager;
    private AccountStorage accountStorage;

    @Override
    public void registerPreLoginHook(PreLoginHook hook) {
        this.preLoginHook = hook;
    }

    @Override
    public void registerPasswordPolicy(PasswordPolicy policy) {
        this.passwordPolicy = policy;
    }

    @Override
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void setAccountStorage(AccountStorage storage) {
        this.accountStorage = storage;
    }

    @Override
    public PasswordPolicy getPasswordPolicy() {
        return passwordPolicy != null ? passwordPolicy : new DefaultPasswordPolicy();
    }

    @Override
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    @Override
    public AccountStorage getAccountStorage() {
        return accountStorage;
    }
}
