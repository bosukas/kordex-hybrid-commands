package io.github.qbosst.kordex.commands.hybrid

import com.kotlindiscord.kord.extensions.checks.notChannelType
import com.kotlindiscord.kord.extensions.checks.types.Check
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.chat.ChatCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Permission
import dev.kord.core.event.Event
import org.koin.core.component.KoinComponent

/**
 * Abstract class representing a hybrid command.
 *
 * @param extension Extension this application command belongs to
 * @param arguments Callable returning an `Arguments` object, if any.
 */
abstract class AbstractHybridCommand<C : HybridCommandContext<*, A>, A : Arguments, S : SlashCommand<*, A>>(
    extension: Extension,
    open val arguments: (() -> A)? = null
) : Command(extension), KoinComponent {

    open val checkList: MutableList<Check<Event>> = mutableListOf()

    /** Command description, as displayed on Discord. **/
    open lateinit var description: String

    /** Command body, to be called when the command is executed. **/
    lateinit var body: suspend C.() -> Unit

    /** Whether this command has a body/action set. **/
    open val hasBody: Boolean get() = ::body.isInitialized

    open var allowInDms: Boolean = extension.allowApplicationCommandInDMs

    protected abstract val context: (CommandContext) -> C

    open val slashCommandSettings: SlashCommandSettings = SlashCommandSettings()

    open val chatCommandSettings: ChatCommandSettings = ChatCommandSettings()

    /** If your bot requires permissions to be able to execute the command, add them using this function. **/
    fun requirePermissions(vararg perms: Permission) {
        perms.forEach { requiredPerms.add(it) }
    }

    /**
     * Define a check which must pass for the command to be executed.
     *
     * A command may have multiple checks - all checks must pass for the command to be executed.
     * Checks will be run in the order that they're defined.
     *
     * This function can be used DSL-style with a given body, or it can be passed one or more
     * predefined functions. See the samples for more information.
     *
     * @param checks Checks to apply to this command.
     */
    fun check(vararg checks: Check<Event>) {
        checks.forEach { checkList.add(it) }
    }

    /**
     * Overloaded check function to allow for DSL syntax.
     *
     * @param check Check to apply to this command.
     */
    fun check(check: Check<Event>) {
        checkList.add(check)
    }

    /** Call this to supply a command [body], to be called when the command is executed. **/
    fun action(action: suspend C.() -> Unit) {
        body = action
    }

    protected open fun applyHybridCommand(slashCommand: SlashCommand<*, *>) {
        slashCommand.name = this.name
        slashCommand.description = this.description
        slashCommand.checkList.addAll(this.checkList)
        slashCommand.requiredPerms.addAll(this.requiredPerms)
        slashCommand.allowInDms = this.allowInDms
    }

    protected open fun applyHybridCommand(chatCommand: ChatCommand<*>) {
        chatCommand.name = this.name
        chatCommand.description = this.description
        chatCommand.checkList.addAll(this.checkList)
        chatCommand.requiredPerms.addAll(this.requiredPerms)

        chatCommand.enabled = this.chatCommandSettings.enabled
        chatCommand.hidden = this.chatCommandSettings.hidden
        chatCommand.aliases = this.chatCommandSettings.aliases

        if (!allowInDms) {
            chatCommand.check {
                notChannelType(ChannelType.DM)
            }
        }
    }

    open class SlashCommandSettings {
        open var enabled: Boolean = true
    }

    open class ChatCommandSettings {
        open var enabled: Boolean = true

        open var hidden: Boolean = false

        open var aliases: Array<String> = arrayOf()
    }
}