package io.github.qbosst.kordex.builders

import dev.kord.common.entity.DiscordMessageReference
import dev.kord.common.entity.MessageFlag
import dev.kord.common.entity.MessageFlags
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.*
import dev.kord.rest.NamedFile
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.MessageComponentBuilder
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.allowedMentions
import dev.kord.rest.json.request.FollowupMessageCreateRequest
import dev.kord.rest.json.request.MessageCreateRequest
import dev.kord.rest.json.request.MultipartFollowupMessageCreateRequest
import dev.kord.rest.json.request.MultipartMessageCreateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class HybridMessageCreateBuilder(public var ephemeral: Boolean) {

    /**
     * The text content of the message.
     */
    public var content: String? = null


    /**
     * An identifier that can be used to validate the message was sent.
     */
    public var nonce: String? = null

    /**
     * Whether this message should be played as a text-to-speech message.
     */
    public var tts: Boolean? = null

    /**
     * The embedded content of the message.
     */
    public val embeds: MutableList<EmbedBuilder> = mutableListOf()

    /**
     * The mentions in this message that are allowed to raise a notification.
     * Setting this to null will default to creating notifications for all mentions.
     */
    public var allowedMentions: AllowedMentionsBuilder? = null

    /**
     * The message components to include in this message.
     */

    public val components: MutableList<MessageComponentBuilder> = mutableListOf()

    /**
     * The files to include as attachments.
     */
    public val files: MutableList<NamedFile> = mutableListOf()


    /**
     * Adds a file with the [name] and [content] to the attachments.
     */
    public fun addFile(name: String, content: InputStream): NamedFile {
        val namedFile = NamedFile(name, content)
        files += namedFile
        return namedFile
    }

    /**
     * Adds a file with the given [path] to the attachments.
     */
    public suspend fun addFile(path: Path): NamedFile = withContext(Dispatchers.IO) {
        addFile(path.fileName.toString(), Files.newInputStream(path))
    }

    fun toFollowupRequest(): MultipartFollowupMessageCreateRequest {
        return MultipartFollowupMessageCreateRequest(
            FollowupMessageCreateRequest(
                content = Optional(content).coerceToMissing(),
                tts = Optional(tts).coerceToMissing().toPrimitive(),
                embeds = Optional(embeds).mapList { it.toRequest() },
                allowedMentions = Optional(allowedMentions).coerceToMissing().map { it.build() },
                components = Optional(components).coerceToMissing().mapList { it.build() },
                flags = Optional(if (ephemeral) MessageFlags(MessageFlag.Ephemeral) else null).coerceToMissing()
            ),
            files
        )
    }

    /**
     * @param messageReference The id of the message being replied to.
     * Requires the [ReadMessageHistory][dev.kord.common.entity.Permission.ReadMessageHistory] permission.
     * @param failIfNotExists whether to error if the referenced message doesn't exist instead of sending as a normal
     * (non-reply) message,defaults to true.
     */
    fun toChatRequest(
        messageReference: Snowflake? = null,
        failIfNotExists: Boolean? = null
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
}


/**
 * Adds an embed to the message, configured by the [block]. A message can have up to 10 embeds.
 */
@OptIn(ExperimentalContracts::class)
public inline fun HybridMessageCreateBuilder.embed(block: EmbedBuilder.() -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    embeds.add(EmbedBuilder().apply(block))
}

/**
 * Configures the mentions that should trigger a mention (aka ping). Not calling this function will result in the default behavior
 * (ping everything), calling this function but not configuring it before the request is build will result in all
 * pings being ignored.
 */
@OptIn(ExperimentalContracts::class)
public inline fun HybridMessageCreateBuilder.allowedMentions(block: AllowedMentionsBuilder.() -> Unit = {}) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    allowedMentions = (allowedMentions ?: AllowedMentionsBuilder()).apply(block)
}

/**
 * Adds an Action Row to the message, configured by the [builder]. A message can have up to 5 action rows.
 */
@OptIn(ExperimentalContracts::class)
public inline fun HybridMessageCreateBuilder.actionRow(builder: ActionRowBuilder.() -> Unit) {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
    components.add(ActionRowBuilder().apply(builder))
}