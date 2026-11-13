package cc.baka9.catseedlogin.common.command;

/** 指令输出的出口，各平台用 {@code §} 文本自行构造消息组件。 */
public interface MessageSink {

  void send(String legacyText);
}
