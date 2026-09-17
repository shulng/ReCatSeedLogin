package cc.baka9.catseedlogin.common.proxy;

import cc.baka9.catseedlogin.common.communication.BaseCommunication;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** 代理端(BungeeCord/Velocity)登录态追踪，统一封装已在登录列表与转发到登录服/保持登录的请求流程。 */
public class ProxyLoginTracker {

  private final List<String> loggedInPlayerList = new CopyOnWriteArrayList<>();
  private final BaseCommunication communication;

  public ProxyLoginTracker(BaseCommunication communication) {
    this.communication = communication;
  }

  /** 玩家当前是否已标记为登录。 */
  public boolean isLoggedIn(String name) {
    return loggedInPlayerList.contains(name);
  }

  /**
   * 向登录服的通信服务确认玩家是否已登录；若已登录则将玩家加入本地登录列表。
   *
   * @return 玩家已登录并已加入列表时返回 true
   */
  public boolean markLoggedIn(String name) {
    if (communication.sendConnectRequest(name) != 1) {
      return false;
    }
    loggedInPlayerList.add(name);
    return true;
  }

  /** 玩家在别处已登录判断（本地标记 + 向登录服确认）。 */
  public boolean isAlreadyLoggedInElsewhere(String name) {
    return loggedInPlayerList.contains(name) && communication.sendConnectRequest(name) == 1;
  }

  /** 移除玩家的本地登录标记。 */
  public void markLoggedOut(String name) {
    loggedInPlayerList.remove(name);
  }

  /** 发送保持登录请求。 */
  public void sendKeepLoggedInRequest(String name) {
    communication.sendKeepLoggedInRequest(name);
  }

  public List<String> getLoggedInPlayers() {
    return loggedInPlayerList;
  }
}