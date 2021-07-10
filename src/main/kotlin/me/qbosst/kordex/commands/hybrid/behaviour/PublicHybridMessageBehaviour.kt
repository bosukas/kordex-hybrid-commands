package me.qbosst.kordex.commands.hybrid.behaviour

import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.interaction.PublicFollowupMessageBehavior
import dev.kord.core.cache.data.toData
import dev.kord.core.entity.Message
import dev.kord.core.entity.Strategizable
import dev.kord.core.supplier.EntitySupplyStrategy
import me.qbosst.kordex.commands.hybrid.builder.HybridMessageModifyBuilder
import me.qbosst.kordex.commands.hybrid.entity.PublicHybridMessage
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface PublicHybridMessageBehaviour: HybridMessageBehaviour {

    suspend fun delete() = if(isInteraction) {
        kord.rest.interaction.deleteFollowupMessage(applicationId!!, token!!, id)
    } else {
        kord.rest.channel.deleteMessage(channelId = channelId, messageId = id)
    }

    override fun withStrategy(strategy: EntitySupplyStrategy<*>): Strategizable = if(isInteraction) {
        PublicFollowupMessageBehavior(id, applicationId!!, token!!, channelId, kord, strategy.supply(kord))
    } else {
        MessageBehavior(channelId, id, kord, strategy)
    }
}

@OptIn(ExperimentalContracts::class)
suspend inline fun PublicHybridMessageBehaviour.edit(
    builder: HybridMessageModifyBuilder.() -> Unit
): PublicHybridMessage {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
    val builder = HybridMessageModifyBuilder().apply(builder)

    val response = if(isInteraction) {
        kord.rest.interaction.modifyFollowupMessage(applicationId!!, token!!, id, builder.toSlashRequest())
    } else {
        kord.rest.channel.editMessage(channelId, id, builder.toMessageRequest())
    }

    return PublicHybridMessage(Message(response.toData(), kord), applicationId, token, kord)
}