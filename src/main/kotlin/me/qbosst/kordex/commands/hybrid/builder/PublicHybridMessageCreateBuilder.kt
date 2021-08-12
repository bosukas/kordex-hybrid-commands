package me.qbosst.kordex.commands.hybrid.builder

import dev.kord.common.entity.DiscordMessageReference
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.*
import dev.kord.rest.builder.component.MessageComponentBuilder
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.PersistentMessageCreateBuilder
import dev.kord.rest.json.request.FollowupMessageCreateRequest
import dev.kord.rest.json.request.MessageCreateRequest
import dev.kord.rest.json.request.MultipartFollowupMessageCreateRequest
import dev.kord.rest.json.request.MultipartMessageCreateRequest
import java.io.InputStream

class PublicHybridMessageCreateBuilder : PersistentMessageCreateBuilder,
    HybridRequestBuilder<MultipartMessageCreateRequest, MultipartFollowupMessageCreateRequest> {

    override var content: String? = null

    override var tts: Boolean? = null

    override val embeds: MutableList<EmbedBuilder> = mutableListOf()

    override var allowedMentions: AllowedMentionsBuilder? = null

    override val components: MutableList<MessageComponentBuilder> = mutableListOf()

    override val files: MutableList<Pair<String, InputStream>> = mutableListOf()

    override fun toMessageRequest(): MultipartMessageCreateRequest = toMessageRequest(null, null, null)

    fun toMessageRequest(
        messageReference: Snowflake? = null,
        failIfNotExists: Boolean? = null,
        nonce: String? = null,
    ): MultipartMessageCreateRequest {
        return MultipartMessageCreateRequest(
            MessageCreateRequest(
                content = Optional(content).coerceToMissing(),
                nonce = Optional(nonce).coerceToMissing(),
                tts = Optional(tts).coerceToMissing().toPrimitive(),
                embeds = Optional(embeds).mapList { it.toRequest() },
                allowedMentions = Optional(allowedMentions).coerceToMissing().map { it.build() },
                messageReference = messageReference?.let {
                    Optional(
                        DiscordMessageReference(
                            OptionalSnowflake.Value(it),
                            failIfNotExists = Optional(failIfNotExists).coerceToMissing().toPrimitive()
                        )
                    )
                } ?: Optional.Missing(),
                components = Optional(components).coerceToMissing().mapList { it.build() }
            ),
            files
        )
    }

    override fun toSlashRequest(): MultipartFollowupMessageCreateRequest {
        return MultipartFollowupMessageCreateRequest(
            FollowupMessageCreateRequest(
                content = Optional(content).coerceToMissing(),
                tts = Optional(tts).coerceToMissing().toPrimitive(),
                embeds = Optional(embeds).mapList { it.toRequest() },
                allowedMentions = Optional(allowedMentions).coerceToMissing().map { it.build() },
                components = Optional(components).coerceToMissing().mapList { it.build() }
            ),
            Optional(files)
        )
    }
}