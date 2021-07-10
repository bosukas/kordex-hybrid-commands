package me.qbosst.kordex.commands.hybrid

import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.commands.GroupCommand
import com.kotlindiscord.kord.extensions.commands.MessageSubCommand
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.slash.SlashGroup
import com.kotlindiscord.kord.extensions.extensions.Extension

class HybridSubCommand<T: Arguments>(
    extension: Extension,
    arguments: (() -> T)? = null,
    val parent: BasicHybridCommand<out Arguments>
): BasicHybridCommand<T>(extension, arguments) {

    fun slashSettings(init: SlashSettings.() -> Unit) {
        slashSettings.apply(init)
    }

    fun messageSettings(init: MessageSettings.() -> Unit) {
        messageSettings.apply(init)
    }

    override fun validate() {
        super.validate()

        if(!hasBody) {
            throw InvalidCommandException(name, "No command action given.")
        }
    }

    fun toMessageCommand(
        parent: GroupCommand<out Arguments>
    ): MessageSubCommand<T> = MessageSubCommand(extension, arguments, parent).apply {
        this.name = this@HybridSubCommand.name
        this.description = this@HybridSubCommand.description
        this.checkList += this@HybridSubCommand.checkList
        this.requiredPerms += this@HybridSubCommand.requiredPerms

        this.enabled = this@HybridSubCommand.messageSettings.enabled
        this.hidden = this@HybridSubCommand.messageSettings.hidden
        this.aliases = this@HybridSubCommand.messageSettings.aliases

        action { this@HybridSubCommand.body.invoke(HybridCommandContext(this)) }
    }

    fun toSlashCommand(
        parent: SlashCommand<out Arguments>? = null,
        group: SlashGroup? = null
    ): SlashCommand<T> = SlashCommand(extension, arguments, parent, group).apply {
        this.name = this@HybridSubCommand.name
        this.description = this@HybridSubCommand.description
        this.checkList += this@HybridSubCommand.checkList
        this.requiredPerms += this@HybridSubCommand.requiredPerms

        this.autoAck = this@HybridSubCommand.slashSettings.autoAck

        action { this@HybridSubCommand.body.invoke(HybridCommandContext(this)) }
    }
}