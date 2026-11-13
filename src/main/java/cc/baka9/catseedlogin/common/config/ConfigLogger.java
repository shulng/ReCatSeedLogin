package cc.baka9.catseedlogin.common.config;

/** 配置加载所需的日志出口，三个平台各自的日志框架在此适配。 */
public interface ConfigLogger {

  void info(String message);

  void warn(String message);
}
