import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env

suspend fun main() {
    val bot = ExtensibleBot(env("token")!!) {
        extensions {
            add(::TestExtension)
        }

        applicationCommands {
            defaultGuild(env("test_guild")!!)
        }

        chatCommands {
            enabled = true
            defaultPrefix = env("prefix") ?: "!!"
        }
    }

    bot.start()
}