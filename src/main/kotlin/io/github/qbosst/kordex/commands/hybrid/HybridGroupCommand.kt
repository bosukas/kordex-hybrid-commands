package io.github.qbosst.kordex.commands.hybrid

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashGroup
import com.kotlindiscord.kord.extensions.commands.chat.ChatGroupCommand
import com.kotlindiscord.kord.extensions.extensions.Extension

abstract class HybridGroupCommand<C : HybridCommandContext<C, A>, A : Arguments, S : SlashCommand<*, A>>(
    extension: Extension,
    arguments: (() -> A)? = null,
    open val parent: HybridCommand<*, *, *>
): AbstractHybridCommand<C, A, S>(extension, arguments) {
    val commands: MutableList<HybridSubCommand<*, *, *>> = mutableListOf()
    override val slashCommandSettings: SlashCommandSettings = SlashCommandSettings()

    protected abstract val slashSubCommand: (parent: SlashGroup) -> S
    protected abstract val slashCommand: (parent: SlashCommand<*, *>) -> S

    fun toSlashGroup(parent: SlashCommand<*, *>): SlashGroup {
        val group = SlashGroup(name, parent)
        group.description = this.description

        commands.forEach { subCommand ->
            if(subCommand.slashCommandSettings.enabled) {
                group.subCommands.add(subCommand.toSlashSubCommand(group = group))
            }
        }

        if(slashCommandSettings.subCommandName != null) {
            group.subCommands.add(toSlashSubCommand(group))
        }

        return group
    }

    fun toSlashSubCommand(parent: SlashGroup): S {
        val slashSubCommand = slashSubCommand(parent)

        applyHybridCommand(slashSubCommand)
        if(slashCommandSettings.subCommandName != null) {
            slashSubCommand.name = slashCommandSettings.subCommandName!!
        }

        if(slashCommandSettings.subCommandDescription != null) {
            slashSubCommand.description = slashCommandSettings.subCommandDescription!!
        }

        slashSubCommand.action { this@HybridGroupCommand.body(this@HybridGroupCommand.context(this)) }

        return slashSubCommand
    }

    fun toChatGroupCommand(parent: ChatGroupCommand<*>): ChatGroupCommand<A> {
        val chatGroupCommand = ChatGroupCommand(extension, arguments, parent)

        applyHybridCommand(chatGroupCommand)

        commands.forEach { subCommand ->
            chatGroupCommand.commands.add(subCommand.toChatSubCommand(chatGroupCommand))
        }

        if(hasBody) {
            chatGroupCommand.action { this@HybridGroupCommand.body(this@HybridGroupCommand.context(this)) }
        } else {
            chatGroupCommand.action { sendHelp() }
        }

        return chatGroupCommand
    }

    fun slashCommandSettings(builder: SlashCommandSettings.() -> Unit) {
        slashCommandSettings.apply(builder)
    }

    fun chatCommandSettings(builder: ChatCommandSettings.() -> Unit) {
        chatCommandSettings.apply(builder)
    }

    class SlashCommandSettings: AbstractHybridCommand.SlashCommandSettings() {
        var subCommandName: String? = null
        var subCommandDescription: String? = null
    }
}