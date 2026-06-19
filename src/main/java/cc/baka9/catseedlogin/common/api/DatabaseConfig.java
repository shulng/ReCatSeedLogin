package cc.baka9.catseedlogin.common.api;

public interface DatabaseConfig {

    boolean isMySQL();

    String getDatabaseHost();
    int getDatabasePort();
    String getDatabaseName();
    String getDatabaseUser();
    String getDatabasePassword();
}
