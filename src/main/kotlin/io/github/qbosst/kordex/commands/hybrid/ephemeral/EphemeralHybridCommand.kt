package io.github.qbosst.kordex.commands.hybrid.ephemeral

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.chat.ChatCommand
import com.kotlindiscord.kord.extensions.commands.chat.ChatCommandContext
import com.kotlindiscord.kord.extensions.commands.chat.ChatGroupCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.event.Event
import io.github.qbosst.kordex.builders.EphemeralHybridMessageCreateBuilder
import io.github.qbosst.kordex.builders.PublicHybridMessageCreateBuilder
import io.github.qbosst.kordex.commands.hybrid.*
import io.github.qbosst.kordex.commands.hybrid.public.PublicHybridCommandContext

class EphemeralHybridCommand<A: Arguments>(
    extension: Extension,
    arguments: (() -> A)?
): HybridCommand<EphemeralHybridCommandContext<A>, A, EphemeralSlashCommand<A>>(extension, arguments) {

    override val context: (CommandContext) -> EphemeralHybridCommandContext<A>
        get() = { EphemeralHybridCommandContext(it) }

    override val slashCommand: () -> EphemeralSlashCommand<A>
        get() = { EphemeralSlashCommand(extension, arguments) }

    override val subSlashCommand: (SlashCommand<*, *>) -> EphemeralSlashCommand<A>
        get() = { EphemeralSlashCommand(extension, arguments, it) }
}