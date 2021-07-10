package me.qbosst.kordex.commands.hybrid.behaviour

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.KordEntity
import dev.kord.core.entity.Strategizable
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.supplier.getChannelOf
import dev.kord.core.supplier.getChannelOfOrNull

interface HybridMessageBehaviour: KordEntity, Strategizable {

    val applicationId: Snowflake?
    val token: String?
    val channelId: Snowflake

    val isInteraction: Boolean get() = applicationId != null && token != null

    val channel: MessageChannelBehavior get() = MessageChannelBehavior(channelId, kord)

    suspend fun getChannel(): MessageChannel = supplier.getChannelOf(channelId)

    suspend fun getChannelOfOrNull(): MessageChannel? = supplier.getChannelOfOrNull(channelId)
}