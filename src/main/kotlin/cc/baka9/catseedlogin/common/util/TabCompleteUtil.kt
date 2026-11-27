package cc.baka9.catseedlogin.common.util

import java.util.Locale

object TabCompleteUtil {
    @JvmStatic fun filter(candidates: Collection<String>?, prefix: String?): List<String> {
        if (candidates.isNullOrEmpty()) return emptyList()
        val input = prefix?.lowercase(Locale.ROOT) ?: ""
        if (input.isEmpty()) return candidates.toList()
        return candidates.filter { it != null && it.lowercase(Locale.ROOT).startsWith(input) }
    }

    @JvmStatic fun filter(candidates: Array<String>?, prefix: String?): List<String> = filter(candidates?.toList(), prefix)
    @JvmStatic fun lastArg(args: Array<String>?): String = if (args.isNullOrEmpty()) "" else args[args.size - 1]
}
