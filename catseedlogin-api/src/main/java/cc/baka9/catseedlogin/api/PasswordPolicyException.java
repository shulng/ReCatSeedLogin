package cc.baka9.catseedlogin.api;

/**
 * Thrown when a password fails validation.
 * Contains a message key for i18n.
 */
public class PasswordPolicyException extends Exception {

    private final String messageKey;
    private final Object[] args;

    public PasswordPolicyException(String messageKey) {
        this(messageKey, new Object[0]);
    }

    public PasswordPolicyException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return args;
    }
}
