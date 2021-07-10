package me.qbosst.kordex.commands.slash

import com.kotlindiscord.kord.extensions.commands.converters.SlashCommandConverter
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommandRegistry
import dev.kord.common.entity.Snowflake

class BetterSlashCommandRegistry: SlashCommandRegistry() {

    override fun register(command: SlashCommand<out Arguments>, guild: Snowflake?): Boolean {
        val locale = bot.settings.i18nBuilder.defaultLocale

        commands.putIfAbsent(guild, mutableListOf())

        // do not check if arguments are valid if slash command does not use them
        if(command.hasBody) {
            val args = command.arguments?.invoke()
            var lastArgRequired = true  // Start with `true` because required args must come first

            args?.args?.forEach { arg ->
                if (arg.converter !is SlashCommandConverter) {
                    error("Argument ${arg.displayName} does not support slash commands.")
                }

                if (arg.converter.required && !lastArgRequired) {
                    error("Required arguments must be placed before non-required arguments.")
                }

                lastArgRequired = arg.converter.required
            }
        }

        val exists = commands[guild]!!.any { it.name == command.getTranslatedName(locale) }

        if (exists) {
            return false
        }

        commands[guild]!!.add(command)

        return true
    }
}