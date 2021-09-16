package io.github.qbosst.kordex.commands.hybrid.public

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashGroup
import com.kotlindiscord.kord.extensions.commands.chat.ChatCommandContext
import com.kotlindiscord.kord.extensions.commands.chat.ChatGroupCommand
import com.kotlindiscord.kord.extensions.commands.chat.ChatSubCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.event.Event
import io.github.qbosst.kordex.builders.PublicHybridMessageCreateBuilder
import io.github.qbosst.kordex.commands.hybrid.AbstractHybridCommand
import io.github.qbosst.kordex.commands.hybrid.HybridSubCommand

class PublicHybridSubCommand<A: Arguments>(
    extension: Extension,
    arguments: (() -> A)?,
    parent: AbstractHybridCommand<*, *, *>
): HybridSubCommand<PublicHybridCommandContext<A>, A, PublicSlashCommand<A>>(extension, arguments, parent) {
    override val slashCommandGroup: (parent: SlashGroup) -> PublicSlashCommand<A>
        get() = { PublicSlashCommand(extension, arguments, parentGroup = it) }

    override val context: (CommandContext) -> PublicHybridCommandContext<A>
        get() = { PublicHybridCommandContext(it) }

    override val slashCommandParent: (parent: SlashCommand<*, *>) -> PublicSlashCommand<A>
        get() = { PublicSlashCommand(extension, arguments, parentCommand = it) }
}