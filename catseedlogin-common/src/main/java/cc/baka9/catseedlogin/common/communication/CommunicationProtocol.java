package cc.baka9.catseedlogin.common.communication;

/**
 * Protocol constants for cross-proxy communication.
 */
public final class CommunicationProtocol {

    /**
     * Current protocol version.
     */
    public static final int VERSION = 1;

    /**
     * Request commands.
     */
    public static final String CMD_CONNECT = "Connect";
    public static final String CMD_KEEP_LOGGED_IN = "KeepLoggedIn";

    /**
     * Response codes.
     */
    public static final byte RESPONSE_SUCCESS = 1;
    public static final byte RESPONSE_FAILURE = 0;

    /**
     * Connection defaults.
     */
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;
    public static final int DEFAULT_READ_TIMEOUT_MS = 3000;

    private CommunicationProtocol() {}
}
