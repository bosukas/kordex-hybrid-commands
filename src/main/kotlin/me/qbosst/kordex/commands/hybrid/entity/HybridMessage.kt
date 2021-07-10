package me.qbosst.kordex.commands.hybrid.entity

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Message
import dev.kord.core.entity.Strategizable
import dev.kord.core.entity.interaction.EphemeralFollowupMessage
import dev.kord.core.entity.interaction.PublicFollowupMessage
import dev.kord.core.supplier.EntitySupplier
import dev.kord.core.supplier.EntitySupplyStrategy
import me.qbosst.kordex.commands.hybrid.behaviour.HybridMessageBehaviour
import me.qbosst.kordex.commands.hybrid.behaviour.PublicHybridMessageBehaviour

sealed class HybridMessage(val message: Message): HybridMessageBehaviour {

    override val id: Snowflake get() = message.id

    override val channelId: Snowflake get() = message.channelId
}

class PublicHybridMessage(
    message: Message,
    override val applicationId: Snowflake?,
    override val token: String?,
    override val kord: Kord,
    override val supplier: EntitySupplier = kord.defaultSupplier
): HybridMessage(message), PublicHybridMessageBehaviour {
    override fun withStrategy(strategy: EntitySupplyStrategy<*>): Strategizable = if(isInteraction) {
        PublicFollowupMessage(message, applicationId!!, token!!, kord, strategy.supply(kord))
    } else {
        Message(message.data, kord, strategy.supply(kord))
    }
}

class EphemeralHybridMessage(
    message: Message,
    override val applicationId: Snowflake?,
    override val token: String?,
    override val kord: Kord,
    override val supplier: EntitySupplier = kord.defaultSupplier
): HybridMessage(message), PublicHybridMessageBehaviour {
    override fun withStrategy(strategy: EntitySupplyStrategy<*>): Strategizable = if(isInteraction) {
        EphemeralFollowupMessage(message, applicationId!!, token!!, kord, strategy.supply(kord))
    } else {
        Message(message.data, kord, strategy.supply(kord))
    }
}

