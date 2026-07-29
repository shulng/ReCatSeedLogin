package cc.baka9.catseedlogin.common.communication;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class ProxyLoginManager {

  private final List<String> loggedInPlayerList = new CopyOnWriteArrayList<>();
  private final Supplier<Integer> connectRequestSupplier;
  private final Runnable keepLoggedInAction;

  public ProxyLoginManager(Supplier<Integer> connectRequestSupplier, Runnable keepLoggedInAction) {
    this.connectRequestSupplier = connectRequestSupplier;
    this.keepLoggedInAction = keepLoggedInAction;
  }

  public boolean isLoggedIn(String playerName) {
    return loggedInPlayerList.contains(playerName);
  }

  public void addPlayer(String playerName) {
    loggedInPlayerList.add(playerName);
  }

  public void removePlayer(String playerName) {
    loggedInPlayerList.remove(playerName);
  }

  public List<String> getLoggedInPlayers() {
    return loggedInPlayerList;
  }

  public int checkLoginStatus(String playerName) {
    if (loggedInPlayerList.contains(playerName)) {
      return connectRequestSupplier.get();
    }
    return 0;
  }

  public boolean verifyAndLogin(String playerName) {
    if (loggedInPlayerList.contains(playerName)) {
      return true;
    }
    int result = connectRequestSupplier.get();
    if (result == 1) {
      loggedInPlayerList.add(playerName);
      return true;
    }
    return false;
  }

  public void onServerConnected(String playerName, String loginServerName, String currentServerName) {
    if (currentServerName.equals(loginServerName) && loggedInPlayerList.contains(playerName)) {
      keepLoggedInAction.run();
    }
  }

  public boolean shouldRedirectToLoginServer(
      String playerName, String targetServerName, String loginServerName) {
    if (targetServerName.equals(loginServerName)) {
      return false;
    }
    if (loggedInPlayerList.contains(playerName)) {
      return false;
    }
    int result = connectRequestSupplier.get();
    if (result == 1) {
      loggedInPlayerList.add(playerName);
      return false;
    }
    return true;
  }
}