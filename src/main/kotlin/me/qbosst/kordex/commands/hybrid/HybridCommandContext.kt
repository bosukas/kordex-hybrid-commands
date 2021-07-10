package me.qbosst.kordex.commands.hybrid

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.MessageCommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommandContext
import com.kotlindiscord.kord.extensions.components.Components
import dev.kord.core.Kord
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.cache.data.MessageData
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.event.Event
import dev.kord.core.event.message.MessageCreateEvent
import me.qbosst.kordex.commands.hybrid.builder.EphemeralHybridMessageCreateBuilder
import me.qbosst.kordex.commands.hybrid.builder.HybridMessageModifyBuilder
import me.qbosst.kordex.commands.hybrid.builder.PublicHybridMessageCreateBuilder
import me.qbosst.kordex.commands.hybrid.entity.EphemeralHybridMessage
import me.qbosst.kordex.commands.hybrid.entity.PublicHybridMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HybridCommandContext<T: Arguments>(val context: CommandContext): KoinComponent {

    private val bot: ExtensibleBot by inject()

    val kord: Kord get() = context.eventObj.kord
    val eventObj: Event get() = context.eventObj

    val channel: MessageChannelBehavior get() = when(context) {
        is SlashCommandContext<*> -> context.channel
        is MessageCommandContext<*> -> context.channel
        else -> error("Unknown context type provided.")
    }

    val guild: Guild? get() = when(context) {
        is SlashCommandContext<*> -> context.guild
        is MessageCommandContext<*> -> context.guild
        else -> error("Unknown context type provided.")
    }

    val member: MemberBehavior? get() = when(context) {
        is SlashCommandContext<*> -> context.member
        is MessageCommandContext<*> -> context.member
        else -> error("Unknown context type provided.")
    }

    val user: UserBehavior? get() = when(context) {
        is SlashCommandContext<*> -> context.user
        is MessageCommandContext<*> -> context.user
        else -> error("Unknown context type provided")
    }

    val message: Message? get() = when(context) {
        is SlashCommandContext<*> -> null
        is MessageCommandContext<*> -> context.message
        else -> error("Unknown context type provided.")
    }

    @Suppress("UNCHECKED_CAST")
    val arguments: T get() = when(context) {
        is SlashCommandContext<*> -> context.arguments
        is MessageCommandContext<*> -> context.arguments
        else -> error("Unknown context type provided.")
    } as T

    suspend fun getPrefix() = when(context) {
        is SlashCommandContext<*> -> "/"
        is MessageCommandContext<*> -> with(bot.settings.messageCommandsBuilder) {
            prefixCallback.invoke(context.eventObj as MessageCreateEvent, defaultPrefix)
        }
        else -> error("Unknown context type provided.")
    }

    /**
     * Note: This will not be ephemeral if [context] is from a [MessageCommandContext]
     */
    suspend inline fun ephemeralFollowUp(
        builder: EphemeralHybridMessageCreateBuilder.() -> Unit
    ): EphemeralHybridMessage {
        val messageBuilder = EphemeralHybridMessageCreateBuilder().apply(builder)

        val (response, interaction) = when(context) {
            is SlashCommandContext<*> -> {
                val interaction = if(!context.acked) context.ack(true) else context.interactionResponse!!

                kord.rest.interaction.createFollowupMessage(
                    interaction.applicationId,
                    interaction.token,
                    messageBuilder.toSlashRequest()
                ) to interaction
            }

            is MessageCommandContext<*> -> {
                val messageId = message?.id

                kord.rest.channel.createMessage(
                    channel.id,
                    when(messageId) {
                        null -> messageBuilder.toMessageRequest()
                        else -> messageBuilder.toMessageRequest(messageId)
                    }
                ) to null
            }

            else -> error("Unknown context type provided")
        }

        val data = MessageData.from(response)
        return EphemeralHybridMessage(Message(data, kord), interaction?.applicationId, interaction?.token, kord)
    }

    suspend inline fun publicFollowUp(builder: PublicHybridMessageCreateBuilder.() -> Unit): PublicHybridMessage {
        val messageBuilder = PublicHybridMessageCreateBuilder().apply(builder)

        val (response, interaction) = when(context) {
            is SlashCommandContext<*> -> {
                val interaction = if(!context.acked) context.ack(false) else context.interactionResponse!!

                kord.rest.interaction.createFollowupMessage(
                    interaction.applicationId,
                    interaction.token,
                    messageBuilder.toSlashRequest()
                ) to interaction
            }

            is MessageCommandContext<*> -> {
                val messageId = message?.id

                kord.rest.channel.createMessage(
                    channel.id,
                    when(messageId) {
                        null -> messageBuilder.toMessageRequest()
                        else -> messageBuilder.toMessageRequest(messageId)
                    }
                ) to null
            }
            else -> error("Unknown context type provided")
        }

        val data = MessageData.from(response)
        return PublicHybridMessage(Message(data, kord), interaction?.applicationId, interaction?.token, kord)
    }

    /**
     * Convenience function for adding components to your message via the [Components] class.
     *
     * @see Components
     */
    suspend fun PublicHybridMessageCreateBuilder.components(
        timeoutSeconds: Long? = null,
        body: suspend Components.() -> Unit
    ): Components {
        val components = Components(context.command.extension)

        body(components)
        setup(components, timeoutSeconds)

        return components
    }

    suspend fun PublicHybridMessageCreateBuilder.setup(
        component: Components,
        timeoutSeconds: Long? = null
    ) = with(component) {
        sortIntoRows()

        for (row in rows.filter { row -> row.isNotEmpty() }) {
            actionRow {
                row.forEach { it.apply(this) }
            }
        }

        startListening(timeoutSeconds)
    }

    suspend fun HybridMessageModifyBuilder.setup(
        component: Components,
        timeoutSeconds: Long? = null
    ) = with(component) {
        sortIntoRows()

        for (row in rows.filter { row -> row.isNotEmpty() }) {
            actionRow {
                row.forEach { it.apply(this) }
            }
        }

        startListening(timeoutSeconds)
    }
}