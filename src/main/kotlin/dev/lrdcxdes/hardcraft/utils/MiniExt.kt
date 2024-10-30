package dev.lrdcxdes.hardcraft.utils

import dev.lrdcxdes.hardcraft.Hardcraft
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player

fun MiniMessage.deserialize(
    key: String,
    vararg placeholders: Pair<String, String>,
    color: String? = "<!italic>"
): Component {
    var t = "<lang:$key"
    for (ph in placeholders) {
        t += ":'${ph.second}'"
    }
    println("deserializing: $color$t>")
    return Hardcraft.minimessage.deserialize("$color$t>")
}

fun MiniMessage.deserializeRaw(
    key: String,
    vararg placeholders: Pair<String, String>,
    color: String? = "<!italic>"
): String {
    var t = "<lang:$key"
    for (ph in placeholders) {
        t += ":${ph.second}"
    }
    return "$color$t>"
}

fun Player.sendMessage(key: String, vararg placeholders: Pair<String, String>, color: String? = "<!italic>") {
    val message = Hardcraft.minimessage.deserialize(key, *placeholders, color = color)
    this.sendMessage(message)
}

class MessageManager {
    fun getMessage(key: String, vararg placeholders: Pair<String, String>, color: String? = "<!italic>"): Component {
        return Hardcraft.minimessage.deserialize(key, *placeholders, color = color)
    }

    fun getMessageRaw(key: String, vararg placeholders: Pair<String, String>, color: String? = "<!italic>"): String {
        return Hardcraft.minimessage.deserializeRaw(key, *placeholders, color = color)
    }
}

private val lazyGlobalMessageManager: MessageManager by lazy {
    MessageManager()
}

val globalMessageManager: MessageManager
    get() = lazyGlobalMessageManager