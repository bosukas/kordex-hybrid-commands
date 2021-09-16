package io.github.qbosst.kordex.commands.hybrid.public

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.interactions.PublicInteractionContext
import com.kotlindiscord.kord.extensions.pagination.PublicFollowUpPaginator
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import dev.kord.core.cache.data.toData
import dev.kord.core.entity.Message
import io.github.qbosst.kordex.builders.PublicHybridMessageCreateBuilder
import io.github.qbosst.kordex.commands.hybrid.HybridCommandContext
import io.github.qbosst.kordex.entity.PublicHybridMessage
import io.github.qbosst.kordex.pagination.PublicHybridFollowupPaginator
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class PublicHybridCommandContext<A: Arguments>(
    context: CommandContext,
): HybridCommandContext<PublicHybridCommandContext<A>, A>(context) {

    @OptIn(ExperimentalContracts::class)
    suspend inline fun respond(
        builder: PublicHybridMessageCreateBuilder.() -> Unit
    ): PublicHybridMessage {
        contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
        val builder = PublicHybridMessageCreateBuilder().apply(builder)

        val (response, interaction) = if(isSlashContext) {
            val context = (slashContext as PublicSlashCommandContext<A>)
            kord.rest.interaction.createFollowupMessage(
                context.interactionResponse.applicationId,
                context.interactionResponse.token,
                builder.toFollowupMessageCreateRequest()
            ) to context.interactionResponse
        } else {
            kord.rest.channel.createMessage(
                chatContext.channel.id,
                builder.toMessageCreateRequest(messageReference = message?.id)
            ) to null
        }

        return PublicHybridMessage(
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
    ): PublicHybridFollowupPaginator {
        val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

        builder(pages)

        return PublicHybridFollowupPaginator(pages, this)
    }
}