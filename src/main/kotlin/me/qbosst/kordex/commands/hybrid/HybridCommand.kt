package me.qbosst.kordex.commands.hybrid

import com.kotlindiscord.kord.extensions.CommandRegistrationException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.commands.GroupCommand
import com.kotlindiscord.kord.extensions.commands.MessageCommand
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import mu.KLogger
import mu.KotlinLogging

private val logger: KLogger = KotlinLogging.logger {}
private const val DISCORD_LIMIT: Int = 25

class HybridCommand<T: Arguments>(
    extension: Extension,
    arguments: (() -> T)? = null
): BasicHybridCommand<T>(extension, arguments) {
    class SlashSettings(settings: ExtensibleBotBuilder.SlashCommandsBuilder): BasicHybridCommand.SlashSettings() {
        /** Guild ID this slash command is to be registered for, if any. **/
        var guild: Snowflake? = settings.defaultGuild

        /**
         * Slash groups cannot have actions, use this to turn the group action into a subcommand or null if you don't
         * want that behaviour
         */
        var subCommandName: String? = null

        /**
         * Slash groups cannot have actions, this will be used for the subcommand's description.
         */
        var subCommandDescription: String? = null

        /** Specify a specific guild for this slash command. **/
        fun guild(guild: Snowflake) {
            this.guild = guild
        }

        /** Specify a specific guild for this slash command. **/
        fun guild(guild: Long) {
            this.guild = Snowflake(guild)
        }

        /** Specify a specific guild for this slash command. **/
        fun guild(guild: GuildBehavior) {
            this.guild = guild.id
        }
    }

    override val slashSettings: SlashSettings = SlashSettings(settings.slashCommandsBuilder)

    val groups: MutableMap<String, HybridGroupCommand<out Arguments>> = mutableMapOf()
    val commands: MutableList<HybridSubCommand<out Arguments>> = mutableListOf()

    fun slashSettings(init: SlashSettings.() -> Unit) {
        slashSettings.apply(init)
    }

    fun messageSettings(init: MessageSettings.() -> Unit) {
        messageSettings.apply(init)
    }

    override fun validate() {
        super.validate()

        if(!hasBody && groups.isEmpty() && commands.isEmpty()) {
            throw InvalidCommandException(name, "No command action or subcommands/groups given.")
        }
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
        if(groups.isNotEmpty()) {
            error("Commands may only contain subcommands or command groups, not both.")
        }

        if(commands.size >= DISCORD_LIMIT) {
            error("Commands may only contain up to $DISCORD_LIMIT top-level subcommands.")
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

    suspend fun <R: Arguments> group(
        arguments: (() -> R)?,
        body: suspend HybridGroupCommand<R>.() -> Unit
    ): HybridGroupCommand<R> {
        val commandObj = HybridGroupCommand(extension, arguments, this)
        body.invoke(commandObj)
        return group(commandObj)
    }

    fun <R: Arguments> group(commandObj: HybridGroupCommand<R>): HybridGroupCommand<R> {
        if(commands.isNotEmpty()) {
            error("Commands may only contain subcommands or command groups, not both.")
        }

        if(groups.size >= DISCORD_LIMIT) {
            error("Commands may only contain up to $DISCORD_LIMIT command groups.")
        }

        if(groups[commandObj.name] != null) {
            error("A command group with the name '${commandObj.name}' has already been registered.")
        }

        try {
            commandObj.validate()
            groups[commandObj.name] = commandObj
        } catch (e: CommandRegistrationException) {
            logger.error(e) { "Failed to register subcommand - $e" }
        } catch (e: InvalidCommandException) {
            logger.error(e) { "Failed to register subcommand - $e" }
        }

        return commandObj
    }

    suspend fun group(
        body: suspend HybridGroupCommand<Arguments>.() -> Unit
    ): HybridGroupCommand<Arguments> = group(null, body)

    fun toMessageCommand(): MessageCommand<T> {
        val commandObj = if(commands.isNotEmpty() || groups.isNotEmpty()) {
            GroupCommand(extension, arguments).apply {
                this.commands.addAll(
                    this@HybridCommand.commands.map { it.toMessageCommand(parent = this) }
                )

                this.commands.addAll(
                    this@HybridCommand.groups.map { (_, it) -> it.toMessageCommand(parent = this) }
                )
            }
        } else {
            MessageCommand(extension, arguments)
        }

        return commandObj.apply {
            this.name = this@HybridCommand.name
            this.description = this@HybridCommand.description
            this.checkList += this@HybridCommand.checkList
            this.requiredPerms += this@HybridCommand.requiredPerms

            this.enabled = this@HybridCommand.messageSettings.enabled
            this.hidden = this@HybridCommand.messageSettings.hidden
            this.aliases = this@HybridCommand.messageSettings.aliases

            if(hasBody) {
                action { this@HybridCommand.body.invoke(HybridCommandContext(this)) }
            } else {
                action { sendHelp() }
            }
        }
    }

    fun toSlashCommand(): SlashCommand<T> = SlashCommand(extension, arguments).apply {
        this.name = this@HybridCommand.name
        this.description = this@HybridCommand.description
        this.checkList += this@HybridCommand.checkList
        this.requiredPerms += this@HybridCommand.requiredPerms

        this.autoAck = this@HybridCommand.slashSettings.autoAck
        this.guild = this@HybridCommand.slashSettings.guild

        when {
            this@HybridCommand.groups.isNotEmpty() -> this.groups.putAll(
                this@HybridCommand.groups
                    .filter { (_, it) -> it.slashSettings.enabled }
                    .map { (name, it) -> name to it.toSlashGroup(parent = this) }
            )

            this@HybridCommand.commands.isNotEmpty() -> this.subCommands.addAll(
                this@HybridCommand.commands
                    .filter { it.slashSettings.enabled }
                    .map { it.toSlashCommand(parent = this) }
            )

            else -> action { this@HybridCommand.body.invoke(HybridCommandContext(this)) }
        }

        if(this@HybridCommand.slashSettings.subCommandName != null && this@HybridCommand.hasBody) {
            this.subCommands.add(toSlashSubCommand(this))
        }
    }

    private fun toSlashSubCommand(
        parent: SlashCommand<out Arguments>
    ): SlashCommand<T> = SlashCommand(extension, arguments, parentCommand = parent).apply {
        this.name = this@HybridCommand.slashSettings.subCommandName!!
        this.description = this@HybridCommand.slashSettings.subCommandDescription ?: this@HybridCommand.description
        this.checkList += this@HybridCommand.checkList
        this.requiredPerms += this@HybridCommand.requiredPerms

        this.autoAck = this@HybridCommand.slashSettings.autoAck

        action { this@HybridCommand.body.invoke(HybridCommandContext(this)) }
    }
}