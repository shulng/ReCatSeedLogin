package cc.baka9.catseedlogin.common.hook;

import cc.baka9.catseedlogin.api.PasswordPolicy;
import cc.baka9.catseedlogin.api.PasswordPolicyException;

import java.util.regex.Pattern;

/**
 * Default password policy.
 * Requires 6-16 characters with both letters and numbers.
 */
public class DefaultPasswordPolicy implements PasswordPolicy {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$"
    );

    @Override
    public void validate(String password) throws PasswordPolicyException {
        if (password == null || password.isEmpty()) {
            throw new PasswordPolicyException("COMMON_PASSWORD_SO_SIMPLE");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new PasswordPolicyException("COMMON_PASSWORD_SO_SIMPLE");
        }
    }
}
