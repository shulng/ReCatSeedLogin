package cc.baka9.catseedlogin.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * TOTP 密钥的对称加解密 (AES-256-GCM)。
 *
 * <p>密码用的是 {@link Crypt} 的 Argon2id 单向哈希，但 TOTP 密钥必须完整展示给玩家去扫码， 所以只能可逆加密。密钥本身单独存放在插件数据目录的 {@code
 * 2fa.key}，与账号库分离—— 只泄露数据库的情况下仍然拿不到 TOTP 密钥。
 */
public final class SecretCodec {

  private static final String ALGORITHM = "AES";
  private static final String TRANSFORMATION = "AES/GCM/NoPadding";
  private static final int KEY_BITS = 256;
  private static final int IV_BYTES = 12;
  private static final int TAG_BITS = 128;
  private static final String KEY_FILE_NAME = "2fa.key";

  private static final SecureRandom RANDOM = new SecureRandom();

  private static volatile SecretKey key;

  private SecretCodec() {}

  /**
   * 加载密钥文件，不存在则生成并写入。应在插件启动时调用一次。
   *
   * @param dataFolder 插件数据目录
   */
  public static synchronized void init(File dataFolder) throws IOException {
    if (key != null) {
      return;
    }
    if (dataFolder == null) {
      throw new IOException("插件数据目录为空，无法初始化 " + KEY_FILE_NAME);
    }
    if (!dataFolder.exists() && !dataFolder.mkdirs()) {
      throw new IOException("无法创建插件数据目录: " + dataFolder);
    }
    File file = new File(dataFolder, KEY_FILE_NAME);
    SecretKey loaded = file.isFile() ? readKey(file) : null;
    if (loaded == null) {
      loaded = generateKey();
      Files.write(
          file.toPath(),
          Base64.getEncoder().encodeToString(loaded.getEncoded()).getBytes(StandardCharsets.UTF_8));
    }
    key = loaded;
  }

  /** 是否已完成初始化。 */
  public static boolean isReady() {
    return key != null;
  }

  /**
   * 加密明文密钥。
   *
   * @return Base64(IV + 密文)
   */
  public static String encrypt(String plain) throws GeneralSecurityException {
    if (key == null) {
      throw new IllegalStateException("SecretCodec 尚未初始化");
    }
    byte[] iv = new byte[IV_BYTES];
    RANDOM.nextBytes(iv);
    Cipher cipher = Cipher.getInstance(TRANSFORMATION);
    cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
    byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
    byte[] out = new byte[iv.length + cipherText.length];
    System.arraycopy(iv, 0, out, 0, iv.length);
    System.arraycopy(cipherText, 0, out, iv.length, cipherText.length);
    return Base64.getEncoder().encodeToString(out);
  }

  /**
   * 解密密文。
   *
   * @return 明文，未初始化 / 密文被篡改 / 密钥文件被换过时返回 null
   */
  public static String decrypt(String stored) {
    if (key == null || stored == null || stored.isEmpty()) {
      return null;
    }
    try {
      byte[] all = Base64.getDecoder().decode(stored);
      if (all.length <= IV_BYTES) {
        return null;
      }
      byte[] cipherText = Arrays.copyOfRange(all, IV_BYTES, all.length);
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(
          Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, Arrays.copyOf(all, IV_BYTES)));
      return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    } catch (Exception e) {
      return null;
    }
  }

  private static SecretKey generateKey() {
    try {
      KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM);
      generator.init(KEY_BITS, RANDOM);
      return generator.generateKey();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("无法生成 AES 密钥", e);
    }
  }

  private static SecretKey readKey(File file) {
    try {
      byte[] raw = Files.readAllBytes(file.toPath());
      byte[] decoded = Base64.getDecoder().decode(new String(raw, StandardCharsets.UTF_8).trim());
      if (decoded.length == 0) {
        return null;
      }
      return new SecretKeySpec(decoded, ALGORITHM);
    } catch (Exception e) {
      return null;
    }
  }
}
