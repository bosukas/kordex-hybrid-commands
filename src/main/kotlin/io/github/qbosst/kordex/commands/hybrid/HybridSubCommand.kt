package io.github.qbosst.kordex.commands.hybrid

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashGroup
import com.kotlindiscord.kord.extensions.commands.chat.ChatGroupCommand
import com.kotlindiscord.kord.extensions.commands.chat.ChatSubCommand
import com.kotlindiscord.kord.extensions.extensions.Extension

abstract class HybridSubCommand<C : HybridCommandContext<C, A>, A : Arguments, S : SlashCommand<*, A>>(
    extension: Extension,
    arguments: (() -> A)? = null,
    val parent: AbstractHybridCommand<*, *, *>
): AbstractHybridCommand<C, A, S>(extension, arguments) {

    protected abstract val slashCommandParent: (SlashCommand<*, *>) -> S
    protected abstract val slashCommandGroup: (SlashGroup) -> S

    fun toChatSubCommand(parent: ChatGroupCommand<*>): ChatSubCommand<A> {
        val command = ChatSubCommand(extension, arguments, parent)

        applyHybridCommand(command)
        command.action { this@HybridSubCommand.body(this@HybridSubCommand.context(this)) }

        return command
    }

    fun toSlashSubCommand(parent: SlashCommand<*, *>): S {
        return applySlashCommand(slashCommandParent(parent))
    }

    fun toSlashSubCommand(group: SlashGroup): S {
        return applySlashCommand(slashCommandGroup(group))
    }

    private fun applySlashCommand(slashCommand: S): S {
        applyHybridCommand(slashCommand)
        slashCommand.action { this@HybridSubCommand.body(this@HybridSubCommand.context(this)) }
        return slashCommand
    }

    fun slashCommandSettings(builder: SlashCommandSettings.() -> Unit) {
        slashCommandSettings.apply(builder)
    }

    fun chatCommandSettings(builder: ChatCommandSettings.() -> Unit) {
        chatCommandSettings.apply(builder)
    }
}