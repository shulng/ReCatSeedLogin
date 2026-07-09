package cc.baka9.catseedlogin.common.communication;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Base communication class for proxy authentication.
 * Fixed from v2:
 * - Proper HMAC-SHA256 instead of raw SHA-256 concatenation
 * - Configurable connection and read timeouts
 * - Better error handling
 */
public abstract class BaseCommunication {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    protected abstract String getProxyHost();
    protected abstract int getProxyPort();
    protected abstract void logError(String message, Exception e);
    protected abstract void logWarning(String message);
    protected abstract String getAuthKey();

    /**
     * Send a connect request to check if a player is logged in.
     *
     * @param playerName the player to check
     * @return 1 if logged in, 0 if not
     */
    public int sendConnectRequest(String playerName) {
        try (Socket socket = createSocket()) {
            BufferedWriter writer = createWriter(socket);

            writer.write(CommunicationProtocol.CMD_CONNECT);
            writer.newLine();
            writer.write(playerName);
            writer.newLine();
            writer.flush();

            int response = socket.getInputStream().read();
            return response == CommunicationProtocol.RESPONSE_SUCCESS ? 1 : 0;
        } catch (IOException e) {
            logError("Failed to send connect request for " + playerName, e);
            return 0;
        }
    }

    /**
     * Send a keep-logged-in request to the proxy.
     *
     * @param playerName the player name
     */
    public void sendKeepLoggedInRequest(String playerName) {
        try (Socket socket = createSocket()) {
            BufferedWriter writer = createWriter(socket);

            String time = String.valueOf(System.currentTimeMillis());
            String hmac = computeHmac(playerName, time, getAuthKey());

            writer.write(CommunicationProtocol.CMD_KEEP_LOGGED_IN);
            writer.newLine();
            writer.write(playerName);
            writer.newLine();
            writer.write(time);
            writer.newLine();
            writer.write(hmac);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            logError("Failed to send keep-logged-in request for " + playerName, e);
        }
    }

    /**
     * Create a socket with configured timeouts.
     */
    protected Socket createSocket() throws IOException {
        Socket socket = new Socket();
        socket.connect(
                new java.net.InetSocketAddress(getProxyHost(), getProxyPort()),
                CommunicationProtocol.DEFAULT_CONNECT_TIMEOUT_MS
        );
        socket.setSoTimeout(CommunicationProtocol.DEFAULT_READ_TIMEOUT_MS);
        return socket;
    }

    protected BufferedWriter createWriter(Socket socket) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    /**
     * Compute HMAC-SHA256 signature.
     * Fixed from v2: uses proper HMAC instead of raw SHA-256 concatenation.
     */
    public static String computeHmac(String... parts) {
        if (parts.length < 2) {
            throw new IllegalArgumentException("HMAC requires at least 2 parts");
        }

        String message = String.join("", parts);
        String key = parts[parts.length - 1]; // last part is the key

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM
            );
            mac.init(keySpec);
            byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
