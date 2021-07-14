package me.qbosst.kordex.commands.hybrid

import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.checks.types.Check
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.AutoAckType
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import dev.kord.common.entity.Permission
import dev.kord.core.Kord
import dev.kord.core.event.Event
import dev.kord.core.event.message.MessageCreateEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class BasicHybridCommand<T: Arguments>(
    val extension: Extension,
    open val arguments: (() -> T)? = null
): KoinComponent {
    open class SlashSettings {
        /** Types of automatic ack to use, if any. **/
        open var autoAck: AutoAckType = AutoAckType.NONE

        /**
         * Whether this command is enabled and can be invoked.
         *
         * Disabled commands cannot be invoked.
         *
         * Unlike message commands, this CANNOT be changed at runtime.
         */
        open var enabled: Boolean = true
    }

    open class MessageSettings {
        /**
         * Whether this command is enabled and can be invoked.
         *
         * Disabled commands cannot be invoked, and won't be shown in help commands.
         *
         * This can be changed at runtime, if commands need to be enabled and disabled dynamically without being
         * reconstructed.
         */
        open var enabled: Boolean = true

        /**
         * Whether to hide this command from help command listings.
         *
         * By default, this is `false` - so the command will be shown.
         */
        open var hidden: Boolean = false

        /**
         * Alternative names that can be used to invoke your command.
         *
         * There's no limit on the number of aliases a command may have, but in the event of an alias matching
         * the [name] of a registered command, the command with the [name] takes priority.
         */
        open var aliases: Array<String> = arrayOf()
    }

    open val slashSettings: SlashSettings = SlashSettings()
    open val messageSettings: MessageSettings = MessageSettings()

    val settings: ExtensibleBotBuilder by inject()

    /** Kord instance, backing the ExtensibleBot. **/
    val kord: Kord by inject()

    /** Sentry adapter, for easy access to Sentry functions. **/
    val sentry: SentryAdapter by inject()

    /**
     * The name of this command, for invocation and help commands.
     */
    lateinit var name: String

    /** Command description, as displayed on Discord. **/
    lateinit var description: String

    lateinit var body: suspend HybridCommandContext<out T>.() -> Unit

    /** Whether this command has a body/action set. **/
    val hasBody: Boolean get() = ::body.isInitialized

    val checkList: MutableList<Check<Event>> = mutableListOf()
    val requiredPerms: MutableSet<Permission> = mutableSetOf()

    /**
     * An internal function used to ensure that all of a command's required arguments are present.
     *
     * @throws InvalidCommandException Thrown when a required argument hasn't been set.
     */
    open fun validate() {
        if(!::name.isInitialized) {
            throw InvalidCommandException(null, "No command name given.")
        }

        if(!::description.isInitialized) {
            throw InvalidCommandException(name, "No command description given.")
        }
    }

    /**
     * Define what will happen when your command is invoked.
     *
     * @param action The body of your command, which will be executed when your command is invoked.
     */
    fun action(action: suspend HybridCommandContext<out T>.() -> Unit) {
        this.body = action
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

    /**
     * Define a simple Boolean check which must pass for the command to be executed.
     *
     * Boolean checks are simple wrappers around the regular check system, allowing you to define a basic check that
     * takes an event object and returns a [Boolean] representing whether it passed. This style of check does not have
     * the same functionality as a regular check, and cannot return a message.
     *
     * A command may have multiple checks - all checks must pass for the command to be executed.
     * Checks will be run in the order that they're defined.
     *
     * This function can be used DSL-style with a given body, or it can be passed one or more
     * predefined functions. See the samples for more information.
     *
     * @param checks Checks to apply to this command.
     */
    fun booleanCheck(vararg checks: suspend (Event) -> Boolean) {
        checks.forEach(::booleanCheck)
    }

    /**
     * Overloaded simple Boolean check function to allow for DSL syntax.
     *
     * Boolean checks are simple wrappers around the regular check system, allowing you to define a basic check that
     * takes an event object and returns a [Boolean] representing whether it passed. This style of check does not have
     * the same functionality as a regular check, and cannot return a message.
     *
     * @param check Check to apply to this command.
     */
    fun booleanCheck(check: suspend (Event) -> Boolean) {
        check {
            if(check(event)) {
                pass()
            } else {
                fail()
            }
        }
    }

    /** If your bot requires permissions to be able to execute the command, add them using this function. **/
    fun requirePermissions(vararg perms: Permission) {
        requiredPerms.addAll(perms)
    }
}