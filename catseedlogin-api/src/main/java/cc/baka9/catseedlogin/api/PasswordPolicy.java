package cc.baka9.catseedlogin.api;

/**
 * Pluggable password policy.
 * Allows customization of password validation rules.
 */
public interface PasswordPolicy {

    /**
     * Validate a password.
     *
     * @param password the password to validate
     * @throws PasswordPolicyException if the password is invalid
     */
    void validate(String password) throws PasswordPolicyException;
}
