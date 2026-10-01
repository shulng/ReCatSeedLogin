package cc.baka9.catseedlogin.bukkit.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;
import org.bukkit.Bukkit;
import org.bukkit.World;

/** 世界相关工具：获取 {@code server.properties} 中配置的主世界。 */
public final class WorldUtil {

  private WorldUtil() {}

  /** 返回 server.properties 中 level-name 指定的世界，解析失败时回退到第一个世界。 */
  public static World getDefaultWorld() {
    File serverProps = new File("server.properties");
    if (!serverProps.exists()) {
      return firstWorld();
    }
    try (InputStream is = new BufferedInputStream(Files.newInputStream(serverProps.toPath()))) {
      Properties props = new Properties();
      props.load(is);
      String worldName = props.getProperty("level-name");
      if (worldName != null) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) return world;
      }
    } catch (Exception e) {
      Bukkit.getLogger().warning("读取 server.properties 失败: " + e.getMessage());
    }
    return firstWorld();
  }

  private static World firstWorld() {
    return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
  }
}
