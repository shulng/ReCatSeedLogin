package cc.baka9.catseedlogin.common.api;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Platform-agnostic plugin context interface.
 * Each platform's main class implements this.
 */
public interface PluginContext {

    File getDataFolder();

    InputStream getResourceAsStream(String name);

    Logger getLogger();
}
