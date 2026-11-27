package cc.baka9.catseedlogin.bukkit.database

import java.sql.Connection
import java.sql.PreparedStatement
import java.util.Arrays

class BufferStatement(private val query: String, private vararg val values: Any?) {
    private val stacktrace = Exception()
    @Throws(java.sql.SQLException::class)
    fun prepareStatement(con: Connection): PreparedStatement = con.prepareStatement(query).also { ps ->
        values.forEachIndexed { index, value -> ps.setObject(index + 1, value) }
    }
    fun getStackTrace(): Array<StackTraceElement> = stacktrace.stackTrace
    override fun toString(): String = "Query: $query, values: ${Arrays.toString(values)}"
}
