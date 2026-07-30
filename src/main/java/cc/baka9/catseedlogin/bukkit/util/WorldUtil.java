package cc.baka9.catseedlogin.bukkit.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldUtil {

  public static World getDefaultWorld(Logger logger) {
    File serverProps = new File("server.properties");
    if (!serverProps.exists()) {
      return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
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
      if (logger != null) {
        logger.warning("读取 server.properties 失败: " + e.getMessage());
      }
    }
    return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
  }
}
