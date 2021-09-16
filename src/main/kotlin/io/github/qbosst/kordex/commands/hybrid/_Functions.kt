package io.github.qbosst.kordex.commands.hybrid

import com.kotlindiscord.kord.extensions.CommandRegistrationException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.commands.Arguments
import io.github.qbosst.kordex.commands.hybrid.ephemeral.EphemeralHybridGroupCommand
import io.github.qbosst.kordex.commands.hybrid.ephemeral.EphemeralHybridSubCommand
import io.github.qbosst.kordex.commands.hybrid.public.PublicHybridGroupCommand
import io.github.qbosst.kordex.commands.hybrid.public.PublicHybridSubCommand
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

suspend fun <R: Arguments> HybridCommand<*, *, *>.publicGroupCommand(
    arguments: () -> R,
    body: suspend PublicHybridGroupCommand<R>.() -> Unit
): PublicHybridGroupCommand<R> {
    val commandObj = PublicHybridGroupCommand(extension, arguments, this)

    body(commandObj)

    return publicGroupCommand(commandObj)
}

suspend fun HybridCommand<*, *, *>.publicGroupCommand(
    body: suspend PublicHybridGroupCommand<Arguments>.() -> Unit
): PublicHybridGroupCommand<Arguments> {
    val commandObj = PublicHybridGroupCommand<Arguments>(extension, null, this)

    body(commandObj)

    return publicGroupCommand(commandObj)
}

fun <R: Arguments> HybridCommand<*, *, *>.publicGroupCommand(
    commandObj: PublicHybridGroupCommand<R>
): PublicHybridGroupCommand<R> {
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

suspend fun <R: Arguments> HybridCommand<*, *, *>.ephemeralGroupCommand(
    arguments: () -> R,
    body: suspend EphemeralHybridGroupCommand<R>.() -> Unit
): EphemeralHybridGroupCommand<R> {
    val commandObj = EphemeralHybridGroupCommand(extension, arguments, this)

    body(commandObj)

    return ephemeralGroupCommand(commandObj)
}

suspend fun HybridCommand<*, *, *>.ephemeralGroupCommand(
    body: suspend EphemeralHybridGroupCommand<Arguments>.() -> Unit
): EphemeralHybridGroupCommand<Arguments> {
    val commandObj = EphemeralHybridGroupCommand<Arguments>(extension, null, this)

    body(commandObj)

    return ephemeralGroupCommand(commandObj)
}

fun <R: Arguments> HybridCommand<*, *, *>.ephemeralGroupCommand(
    commandObj: EphemeralHybridGroupCommand<R>
): EphemeralHybridGroupCommand<R> {
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

suspend fun <R: Arguments> HybridCommand<*, *, *>.publicSubCommand(
    arguments: () -> R,
    body: suspend PublicHybridSubCommand<R>.() -> Unit
): PublicHybridSubCommand<R> {
    val commandObj = PublicHybridSubCommand(extension, arguments, this)

    body(commandObj)

    return publicSubCommand(commandObj)
}

suspend fun HybridCommand<*, *, *>.publicSubCommand(
    body: suspend PublicHybridSubCommand<Arguments>.() -> Unit
): PublicHybridSubCommand<Arguments> {
    val commandObj = PublicHybridSubCommand<Arguments>(extension, null, this)

    body(commandObj)

    return publicSubCommand(commandObj)
}

fun <R: Arguments> HybridCommand<*, *, *>.publicSubCommand(
    commandObj: PublicHybridSubCommand<R>
): PublicHybridSubCommand<R> {
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

suspend fun <R: Arguments> HybridCommand<*, *, *>.ephemeralSubCommand(
    arguments: () -> R,
    body: suspend EphemeralHybridSubCommand<R>.() -> Unit
): EphemeralHybridSubCommand<R> {
    val commandObj = EphemeralHybridSubCommand(extension, arguments, this)

    body(commandObj)

    return ephemeralSubCommand(commandObj)
}

suspend fun HybridCommand<*, *, *>.ephemeralSubCommand(
    body: suspend EphemeralHybridSubCommand<Arguments>.() -> Unit
): EphemeralHybridSubCommand<Arguments> {
    val commandObj = EphemeralHybridSubCommand<Arguments>(extension, null, this)

    body(commandObj)

    return ephemeralSubCommand(commandObj)
}

fun <R: Arguments> HybridCommand<*, *, *>.ephemeralSubCommand(
    commandObj: EphemeralHybridSubCommand<R>
): EphemeralHybridSubCommand<R> {
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

suspend fun <R: Arguments> HybridGroupCommand<*, *, *>.publicSubCommand(
    arguments: () -> R,
    body: suspend PublicHybridSubCommand<R>.() -> Unit
): PublicHybridSubCommand<R> {
    val commandObj = PublicHybridSubCommand(extension, arguments, this)

    body(commandObj)

    return publicSubCommand(commandObj)
}

suspend fun HybridGroupCommand<*, *, *>.publicSubCommand(
    body: suspend PublicHybridSubCommand<Arguments>.() -> Unit
): PublicHybridSubCommand<Arguments> {
    val commandObj = PublicHybridSubCommand<Arguments>(extension, null, this)

    body(commandObj)

    return publicSubCommand(commandObj)
}

fun <R: Arguments> HybridGroupCommand<*, *, *>.publicSubCommand(
    commandObj: PublicHybridSubCommand<R>
): PublicHybridSubCommand<R> {
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

suspend fun <R: Arguments> HybridGroupCommand<*, *, *>.ephemeralSubCommand(
    arguments: () -> R,
    body: suspend EphemeralHybridSubCommand<R>.() -> Unit
): EphemeralHybridSubCommand<R> {
    val commandObj = EphemeralHybridSubCommand(extension, arguments, this)

    body(commandObj)

    return ephemeralSubCommand(commandObj)
}

suspend fun HybridGroupCommand<*, *, *>.ephemeralSubCommand(
    body: suspend EphemeralHybridSubCommand<Arguments>.() -> Unit
): EphemeralHybridSubCommand<Arguments> {
    val commandObj = EphemeralHybridSubCommand<Arguments>(extension, null, this)

    body(commandObj)

    return ephemeralSubCommand(commandObj)
}

fun <R: Arguments> HybridGroupCommand<*, *, *>.ephemeralSubCommand(
    commandObj: EphemeralHybridSubCommand<R>
): EphemeralHybridSubCommand<R> {
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