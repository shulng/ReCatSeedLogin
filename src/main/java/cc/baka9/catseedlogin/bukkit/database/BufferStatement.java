package cc.baka9.catseedlogin.bukkit.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

public class BufferStatement {
    private final Object[] values;
    private final String query;

    public BufferStatement(String query, Object... values) {
        this.query = query;
        this.values = values;
    }

    public PreparedStatement prepareStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(query);
        for (int i = 0; i < values.length; i++) {
            ps.setObject(i + 1, values[i]);
        }
        return ps;
    }

    @Override
    public String toString() {
        return "BufferStatement{query='" + query + "'}";
    }
}
