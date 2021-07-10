package me.qbosst.kordex.commands.hybrid

import com.kotlindiscord.kord.extensions.CommandRegistrationException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.commands.GroupCommand
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.slash.SlashGroup
import com.kotlindiscord.kord.extensions.extensions.Extension
import mu.KLogger
import mu.KotlinLogging

private val logger: KLogger = KotlinLogging.logger {}
private const val DISCORD_LIMIT: Int = 10

class HybridGroupCommand<T: Arguments>(
    extension: Extension,
    arguments: (() -> T)? = null,
    val parent: HybridCommand<out Arguments>
): BasicHybridCommand<T>(extension, arguments) {
    class SlashSettings: BasicHybridCommand.SlashSettings() {
        /**
         * Slash groups cannot have actions, use this to turn the group action into a subcommand or null if you don't
         * want that behaviour
         */
        var subCommandName: String? = null

        /**
         * Slash groups cannot have actions, this will be used for the subcommand's description.
         */
        var subCommandDescription: String? = null
    }

    override val slashSettings: SlashSettings = SlashSettings()
    val commands: MutableList<HybridSubCommand<out Arguments>> = mutableListOf()

    override fun validate() {
        super.validate()

        if(commands.isEmpty()) {
            throw InvalidCommandException(name, "Command groups must contain at least one subcommand.")
        }
    }

    fun slashSettings(init: SlashSettings.() -> Unit) {
        slashSettings.apply(init)
    }

    fun messageSettings(init: MessageSettings.() -> Unit) {
        messageSettings.apply(init)
    }

    suspend fun <R: Arguments> subCommand(
        arguments: (() -> R)?,
        body: suspend HybridSubCommand<R>.() -> Unit
    ): HybridSubCommand<R> {
        val commandObj = HybridSubCommand(extension, arguments, this)
        body.invoke(commandObj)
        return subCommand(commandObj)
    }

    fun <R: Arguments> subCommand(commandObj: HybridSubCommand<R>): HybridSubCommand<R> {
        if(commands.size >= DISCORD_LIMIT) {
            error("Groups may only contain up to 10 subcommands.")
        }

        try {
            commandObj.validate()
            commands.add(commandObj)
        } catch (e: CommandRegistrationException) {
            logger.error(e) { "Failed to register subcommand - $e" }
        } catch (e: InvalidCommandException) {
            logger.error(e) { "Failed to register subcommand - $e" }
        }

        return commandObj
    }

    suspend fun subCommand(
        body: suspend HybridSubCommand<Arguments>.() -> Unit
    ): HybridSubCommand<Arguments> = subCommand(null, body)

    fun toMessageCommand(
        parent: GroupCommand<out Arguments>
    ): GroupCommand<T> = GroupCommand(extension, arguments, parent).apply {
        this.name = this@HybridGroupCommand.name
        this.description = this@HybridGroupCommand.description
        this.checkList += this@HybridGroupCommand.checkList
        this.requiredPerms += this@HybridGroupCommand.requiredPerms

        this.enabled = this@HybridGroupCommand.messageSettings.enabled
        this.hidden = this@HybridGroupCommand.messageSettings.hidden
        this.aliases = this@HybridGroupCommand.messageSettings.aliases

        this.commands.addAll(
            this@HybridGroupCommand.commands.map { it.toMessageCommand(parent = this) }
        )

        if(hasBody) {
            action { this@HybridGroupCommand.body.invoke(HybridCommandContext(this)) }
        } else {
            action { sendHelp() }
        }
    }

    fun toSlashGroup(parent: SlashCommand<out Arguments>): SlashGroup = SlashGroup(name, parent).apply {
        this.description = this@HybridGroupCommand.description

        this.subCommands.addAll(
            this@HybridGroupCommand.commands
                .filter { it.slashSettings.enabled }
                .map { it.toSlashCommand(group = this) }
        )

        if(this@HybridGroupCommand.slashSettings.subCommandName != null && this@HybridGroupCommand.hasBody) {
            this.subCommands.add(toSlashCommand(this))
        }
    }

    private fun toSlashCommand(
        parent: SlashGroup
    ): SlashCommand<T> = SlashCommand(extension, arguments, parentGroup = parent).apply {
        this.name = this@HybridGroupCommand.slashSettings.subCommandName!!
        this.description = this@HybridGroupCommand.slashSettings.subCommandDescription
            ?: this@HybridGroupCommand.description
        this.checkList += this@HybridGroupCommand.checkList
        this.requiredPerms += this@HybridGroupCommand.requiredPerms

        this.autoAck = this@HybridGroupCommand.slashSettings.autoAck

        action { this@HybridGroupCommand.body.invoke(HybridCommandContext(this)) }
    }
}