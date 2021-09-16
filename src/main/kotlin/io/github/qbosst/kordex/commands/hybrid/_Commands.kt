package io.github.qbosst.kordex.commands.hybrid

import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.checks.types.Check
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import dev.kord.core.event.Event
import io.github.qbosst.kordex.commands.hybrid.ephemeral.EphemeralHybridCommand
import io.github.qbosst.kordex.commands.hybrid.public.PublicHybridCommand
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Define a check which must pass for a hybrid command to be executed. This check will be applied to all hybrid
 * commands in this extension.
 *
 * A hybrid command may have multiple checks - all checks must pass for the command to be executed.
 * Checks will be run in the order that they're defined.
 *
 * This function can be used DSL-style with a given body, or it can be passed one or more predefined functions.
 * See the samples for more information.
 *
 * @param checks Checks to apply to all hybrid commands.
 */
fun Extension.hybridCheck(vararg checks: Check<Event>) {
    checks.forEach {
        slashCommandChecks.add(it)
        messageCommandChecks.add(it)
    }
}

/**
 * Overloaded hybrid command check function to allow for DSL syntax.
 *
 * @param check Check to apply to all hybrid commands.
 */
fun Extension.hybridCheck(check: Check<Event>) {
    slashCommandChecks.add(check)
    messageCommandChecks.add(check)
}

/** Register a public hybrid command, DSL-style. **/
@ExtensionDSL
suspend fun <T: Arguments> Extension.publicHybridCommand(
    arguments: () -> T,
    body: suspend PublicHybridCommand<T>.() -> Unit
): PublicHybridCommand<T> {
    val commandObj = PublicHybridCommand(this, arguments)
    body(commandObj)

    return publicHybridCommand(commandObj)
}

/** Register a public hybrid command, DSL-style. **/
@ExtensionDSL
suspend fun Extension.publicHybridCommand(
    body: suspend PublicHybridCommand<Arguments>.() -> Unit
): PublicHybridCommand<Arguments> {
    val commandObj = PublicHybridCommand<Arguments>(this, null)
    body(commandObj)

    return publicHybridCommand(commandObj)
}

/** Register a custom instance of a public hybrid command. **/
@ExtensionDSL
suspend fun <T: Arguments> Extension.publicHybridCommand(commandObj: PublicHybridCommand<T>): PublicHybridCommand<T> {
    val chatCommand = commandObj.toChatCommand()
    chatCommand(chatCommand)

    val slashCommand = commandObj.toSlashCommand()
    publicSlashCommand(slashCommand)

    return commandObj
}

/** Register a public hybrid command, DSL-style. **/
@ExtensionDSL
suspend fun <T: Arguments> Extension.ephemeralHybridCommand(
    arguments: () -> T,
    body: suspend EphemeralHybridCommand<T>.() -> Unit
): EphemeralHybridCommand<T> {
    val commandObj = EphemeralHybridCommand(this, arguments)
    body(commandObj)

    return ephemeralHybridCommand(commandObj)
}

/** Register a public hybrid command, DSL-style. **/
@ExtensionDSL
suspend fun Extension.ephemeralHybridCommand(
    body: suspend EphemeralHybridCommand<Arguments>.() -> Unit
): EphemeralHybridCommand<Arguments> {
    val commandObj = EphemeralHybridCommand<Arguments>(this, null)
    body(commandObj)

    return ephemeralHybridCommand(commandObj)
}

/** Register a custom instance of a public hybrid command. **/
@ExtensionDSL
suspend fun <T: Arguments> Extension.ephemeralHybridCommand(commandObj: EphemeralHybridCommand<T>): EphemeralHybridCommand<T> {
    val chatCommand = commandObj.toChatCommand()
    chatCommand(chatCommand)

    val slashCommand = commandObj.toSlashCommand()
    ephemeralSlashCommand(slashCommand)

    return commandObj
}
