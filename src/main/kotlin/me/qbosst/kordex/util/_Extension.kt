package me.qbosst.kordex.util

import com.kotlindiscord.kord.extensions.CommandRegistrationException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.event.Event
import me.qbosst.kordex.commands.hybrid.HybridCommand
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@ExtensionDSL
suspend fun <T: Arguments> Extension.hybridCommand(
    arguments: (() -> T)?,
    body: suspend HybridCommand<T>.() -> Unit
): HybridCommand<T> {
    val hybridCommandObj = HybridCommand(this, arguments)
    body.invoke(hybridCommandObj)

    return hybridCommand(hybridCommandObj)
}

suspend fun <T: Arguments> Extension.hybridCommand(commandObj: HybridCommand<T>): HybridCommand<T> {
    try {
        commandObj.validate()

        // create a message command
        val messageCommandObj = commandObj.toMessageCommand()
        command(messageCommandObj)

        // create a slash command
        val slashCommandObj = commandObj.toSlashCommand()
        slashCommand(slashCommandObj)

    } catch (e: CommandRegistrationException) {
        logger.error(e) { "Failed to register command - $e" }
    } catch (e: InvalidCommandException) {
        logger.error(e) { "Failed to register command - $e" }
    }

    return commandObj
}

@ExtensionDSL
suspend fun Extension.hybridCommand(
    body: suspend HybridCommand<Arguments>.() -> Unit
): HybridCommand<Arguments> = hybridCommand(null, body)

@ExtensionDSL
fun Extension.hybridCheck(body: suspend (Event) -> Boolean) {
    commandChecks.add(body)
    slashCommandChecks.add(body)
}

@ExtensionDSL
fun Extension.hybridCheck(vararg checks: suspend (Event) -> Boolean) {
    commandChecks.addAll(checks)
    slashCommandChecks.addAll(checks)
}