package io.github.qbosst.kordex.commands.hybrid.ephemeral

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashGroup
import com.kotlindiscord.kord.extensions.extensions.Extension
import io.github.qbosst.kordex.commands.hybrid.*

class EphemeralHybridGroupCommand<A: Arguments>(
    extension: Extension,
    arguments: (() -> A)?,
    parent: HybridCommand<*, *, *>
): HybridGroupCommand<EphemeralHybridCommandContext<A>, A, EphemeralSlashCommand<A>>(extension, arguments, parent) {

    override val slashSubCommand: (parent: SlashGroup) -> EphemeralSlashCommand<A>
        get() = { EphemeralSlashCommand(extension, arguments, parentGroup = it) }

    override val context: (CommandContext) -> EphemeralHybridCommandContext<A>
        get() = { EphemeralHybridCommandContext(it) }

    override val slashCommand: (parent: SlashCommand<*, *>) -> EphemeralSlashCommand<A>
        get() = { EphemeralSlashCommand(extension, arguments, parentCommand = it) }
}