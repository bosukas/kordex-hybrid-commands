package io.github.qbosst.kordex.commands.hybrid.ephemeral

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashGroup
import com.kotlindiscord.kord.extensions.commands.chat.ChatCommandContext
import com.kotlindiscord.kord.extensions.commands.chat.ChatGroupCommand
import com.kotlindiscord.kord.extensions.commands.chat.ChatSubCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.event.Event
import io.github.qbosst.kordex.builders.EphemeralHybridMessageCreateBuilder
import io.github.qbosst.kordex.builders.PublicHybridMessageCreateBuilder
import io.github.qbosst.kordex.commands.hybrid.AbstractHybridCommand
import io.github.qbosst.kordex.commands.hybrid.HybridSubCommand
import io.github.qbosst.kordex.commands.hybrid.public.PublicHybridCommandContext

class EphemeralHybridSubCommand<A: Arguments>(
    extension: Extension,
    arguments: (() -> A)?,
    parent: AbstractHybridCommand<*, *, *>
): HybridSubCommand<EphemeralHybridCommandContext<A>, A, EphemeralSlashCommand<A>>(extension, arguments, parent) {

    override val slashCommandGroup: (parent: SlashGroup) -> EphemeralSlashCommand<A>
        get() = { EphemeralSlashCommand(extension, arguments, parentGroup = it) }

    override val context: (CommandContext) -> EphemeralHybridCommandContext<A>
        get() = { EphemeralHybridCommandContext(it) }

    override val slashCommandParent: (parent: SlashCommand<*, *>) -> EphemeralSlashCommand<A>
        get() = { EphemeralSlashCommand(extension, arguments, parentCommand = it) }
}