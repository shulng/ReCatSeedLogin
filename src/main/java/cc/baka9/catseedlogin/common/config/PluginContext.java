package cc.baka9.catseedlogin.common.config;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * 平台无关的插件上下文接口，用于打破 BungeeConfigManager 与 PluginMain 之间的循环依赖。
 */
public interface PluginContext {
    File getDataFolder();

    InputStream getResourceAsStream(String name);

    Logger getLogger();
}
