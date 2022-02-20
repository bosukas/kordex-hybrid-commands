package io.github.qbosst.kordex.commands.hybrid.public

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import io.github.qbosst.kordex.commands.hybrid.*

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