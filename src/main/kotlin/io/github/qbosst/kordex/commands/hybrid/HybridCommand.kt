package io.github.qbosst.kordex.commands.hybrid

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.chat.ChatCommand
import com.kotlindiscord.kord.extensions.commands.chat.ChatGroupCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior

abstract class HybridCommand<C : HybridCommandContext<C, A>, A : Arguments, S : SlashCommand<*, A>>(
    extension: Extension,
    arguments: (() -> A)? = null
): AbstractHybridCommand<C, A, S>(extension, arguments) {
    val groups: MutableMap<String, HybridGroupCommand<*, *, *>> = mutableMapOf()
    val commands: MutableList<HybridSubCommand<*, *, *>> = mutableListOf()
    override val slashCommandSettings: SlashCommandSettings = SlashCommandSettings()

    protected abstract val slashCommand: () -> S
    protected abstract val subSlashCommand: (parent: SlashCommand<*, *>) -> S

    fun toChatCommand(): ChatCommand<A> {
        val command = if(commands.isNotEmpty() || groups.isNotEmpty()) {
            ChatGroupCommand(extension, arguments)
        } else {
            ChatCommand(extension, arguments)
        }

        applyHybridCommand(command)

        if(groups.isNotEmpty()) groups.values.forEach { group ->
            (command as ChatGroupCommand).commands.add(group.toChatGroupCommand(command))
        } else if(commands.isNotEmpty()) commands.forEach { subCommand ->
            (command as ChatGroupCommand).commands.add(subCommand.toChatSubCommand(command))
        }

        if(hasBody) {
            command.action { this@HybridCommand.body(this@HybridCommand.context(this)) }
        } else {
            command.action { sendHelp() }
        }

        return command
    }

    fun toSlashCommand(): S {
        val slashCommand = slashCommand()

        applyHybridCommand(slashCommand)

        when {
            groups.isNotEmpty() -> groups.values.forEach { group ->
                if(group.slashCommandSettings.enabled) {
                    slashCommand.groups[group.name] = group.toSlashGroup(slashCommand)
                }
            }
            commands.isNotEmpty() -> commands.forEach { subCommand ->
                if(subCommand.slashCommandSettings.enabled) {
                    slashCommand.subCommands.add(subCommand.toSlashSubCommand(slashCommand))
                }
            }
            else -> slashCommand.action { this@HybridCommand.body(this@HybridCommand.context(this)) }
        }

        if(slashCommandSettings.subCommandName != null) {
            slashCommand.subCommands.add(toSlashSubCommand(slashCommand))
        }

        return slashCommand
    }

    fun toSlashSubCommand(parent: SlashCommand<*, *>): S {
        val slashSubCommand = subSlashCommand(parent)

        applyHybridCommand(slashSubCommand)
        if(slashCommandSettings.subCommandName != null) {
            slashSubCommand.name = slashCommandSettings.subCommandName!!
        }

        if(slashCommandSettings.subCommandDescription != null) {
            slashSubCommand.description = slashCommandSettings.subCommandDescription!!
        }

        slashSubCommand.action { this@HybridCommand.body(this@HybridCommand.context(this)) }

        return slashSubCommand
    }

    override fun applyHybridCommand(slashCommand: SlashCommand<*, *>) {
        super.applyHybridCommand(slashCommand)

        slashCommand.guildId = this.slashCommandSettings.guildId
    }

    fun slashCommandSettings(builder: SlashCommandSettings.() -> Unit) {
        slashCommandSettings.apply(builder)
    }

    fun chatCommandSettings(builder: ChatCommandSettings.() -> Unit) {
        chatCommandSettings.apply(builder)
    }

    inner class SlashCommandSettings: AbstractHybridCommand.SlashCommandSettings() {
        var subCommandName: String? = null
        var subCommandDescription: String? = null

        var guildId: Snowflake? = settings.applicationCommandsBuilder.defaultGuild

        fun guild(guild: Snowflake) {
            this.guildId = guild
        }

        fun guild(guild: Long) {
            this.guildId = Snowflake(guild)
        }

        fun guild(guild: GuildBehavior) {
            this.guildId = guild.id
        }
    }
}