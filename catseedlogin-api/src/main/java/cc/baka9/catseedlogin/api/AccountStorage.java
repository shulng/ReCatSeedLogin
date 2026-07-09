package cc.baka9.catseedlogin.api;

import java.util.List;
import java.util.Optional;

/**
 * Account storage interface for database abstraction.
 * Allows other plugins to provide custom database implementations.
 */
public interface AccountStorage {

    /**
     * Get an account by player name.
     */
    Optional<AccountData> getAccount(String name);

    /**
     * Get all accounts.
     */
    List<AccountData> getAllAccounts();

    /**
     * Save (insert or update) an account.
     */
    void saveAccount(AccountData data);

    /**
     * Delete an account.
     */
    void deleteAccount(String name);

    /**
     * Simple account data representation.
     */
    interface AccountData {
        String getName();
        String getPasswordHash();
        String getPasswordVersion();
        String getEmail();
        List<String> getIps();
        long getLastAction();
        String getLocation();
    }
}
