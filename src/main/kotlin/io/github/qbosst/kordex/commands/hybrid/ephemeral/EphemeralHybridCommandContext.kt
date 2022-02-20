package io.github.qbosst.kordex.commands.hybrid.ephemeral

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import dev.kord.core.cache.data.toData
import dev.kord.core.entity.Message
import io.github.qbosst.kordex.builders.HybridMessageCreateBuilder
import io.github.qbosst.kordex.commands.hybrid.HybridCommandContext
import io.github.qbosst.kordex.entity.EphemeralHybridMessage
import io.github.qbosst.kordex.pagination.EphemeralHybridFollowupPaginator
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class EphemeralHybridCommandContext<A: Arguments>(
    context: CommandContext,
): HybridCommandContext<EphemeralHybridCommandContext<A>, A>(context) {

    @OptIn(ExperimentalContracts::class)
    suspend inline fun respond(
        builder: HybridMessageCreateBuilder.() -> Unit
    ): EphemeralHybridMessage {
        contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
        val builder = HybridMessageCreateBuilder(true).apply(builder)

        val (response, interaction) = if(isSlashContext) {
            val context = (slashContext as EphemeralSlashCommandContext<A>)
            kord.rest.interaction.createFollowupMessage(
                context.interactionResponse.applicationId,
                context.interactionResponse.token,
                builder.toFollowupRequest()
            ) to context.interactionResponse
        } else {
            kord.rest.channel.createMessage(
                chatContext.channel.id,
                builder.toChatRequest(messageReference = message?.id)
            ) to null
        }

        return EphemeralHybridMessage(
            Message(response.toData(), kord),
            interaction?.applicationId,
            interaction?.token,
            kord
        )
    }

    inline fun respondingPaginator(
        defaultGroup: String = "",
        locale: Locale? = null,
        builder: (PaginatorBuilder).() -> Unit
    ): EphemeralHybridFollowupPaginator {
        val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

        builder(pages)

        return EphemeralHybridFollowupPaginator(pages, this)
    }
}