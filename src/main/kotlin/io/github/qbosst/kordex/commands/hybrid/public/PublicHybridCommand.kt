package io.github.qbosst.kordex.commands.hybrid.public

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashGroup
import com.kotlindiscord.kord.extensions.commands.chat.ChatCommand
import com.kotlindiscord.kord.extensions.commands.chat.ChatCommandContext
import com.kotlindiscord.kord.extensions.commands.chat.ChatGroupCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.event.Event
import io.github.qbosst.kordex.builders.PublicHybridMessageCreateBuilder
import io.github.qbosst.kordex.commands.hybrid.*
import io.github.qbosst.kordex.commands.hybrid.ephemeral.EphemeralHybridCommandContext

class PublicHybridCommand<A: Arguments>(
    extension: Extension,
    arguments: (() -> A)?
): HybridCommand<PublicHybridCommandContext<A>, A, PublicSlashCommand<A>>(extension, arguments) {

    override val context: (CommandContext) -> PublicHybridCommandContext<A>
        get() = { PublicHybridCommandContext(it) }

    override val slashCommand: () -> PublicSlashCommand<A>
        get() = { PublicSlashCommand(extension, arguments) }

    override val subSlashCommand: (SlashCommand<*, *>) -> PublicSlashCommand<A>
        get() = { PublicSlashCommand(extension, arguments, it) }
}