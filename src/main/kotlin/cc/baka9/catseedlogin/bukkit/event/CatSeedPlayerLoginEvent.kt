package cc.baka9.catseedlogin.bukkit.event

import java.util.Optional
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class CatSeedPlayerLoginEvent(
    val player: Player,
    email: String?,
    val result: Result
) : Event() {
    private val emailValue = email

    override fun getHandlers(): HandlerList = handlers

    fun getEmail(): Optional<String> = Optional.ofNullable(emailValue)

    enum class Result {
        SUCCESS,
        FAIL
    }

    companion object {
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlers
    }
}
