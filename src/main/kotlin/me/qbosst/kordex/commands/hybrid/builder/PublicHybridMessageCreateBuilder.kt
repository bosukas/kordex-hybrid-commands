package me.qbosst.kordex.commands.hybrid.builder

import dev.kord.common.entity.DiscordMessageReference
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.OptionalBoolean
import dev.kord.common.entity.optional.OptionalSnowflake
import dev.kord.common.entity.optional.delegate.delegate
import dev.kord.common.entity.optional.map
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.MessageComponentBuilder
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import dev.kord.rest.builder.message.EmbedBuilder
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

class PublicHybridMessageCreateBuilder :
    HybridRequestBuilder<MultipartMessageCreateRequest, MultipartFollowupMessageCreateRequest> {

    private var _content: Optional<String> = Optional.Missing()
    var content: String? by ::_content.delegate()

    private var _tts: OptionalBoolean = OptionalBoolean.Missing
    var tts: Boolean? by ::_tts.delegate()

    private var _allowedMentions: Optional<AllowedMentionsBuilder> = Optional.Missing()
    var allowedMentions: AllowedMentionsBuilder? by ::_allowedMentions.delegate()

    private var _embed: Optional<EmbedBuilder> = Optional.Missing()
    var embed: EmbedBuilder? by ::_embed.delegate()

    val files: MutableList<Pair<String, InputStream>> = mutableListOf()

    val components: MutableList<MessageComponentBuilder> = mutableListOf()

    fun addFile(name: String, content: InputStream) {
        files += name to content
    }

    suspend fun addFile(path: Path) = withContext(Dispatchers.IO) {
        addFile(path.fileName.toString(), Files.newInputStream(path))
    }

    @OptIn(ExperimentalContracts::class)
    inline fun actionRow(builder: ActionRowBuilder.() -> Unit) {
        contract {
            callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
        }

        components.add(ActionRowBuilder().apply(builder))
    }


    /**
     * Configures the mentions that should trigger a mention (aka ping). Not calling this function will result in the default behavior
     * (ping everything), calling this function but not configuring it before the request is build will result in all
     * pings being ignored.
     */
    @OptIn(ExperimentalContracts::class)
    inline fun allowedMentions(block: AllowedMentionsBuilder.() -> Unit = {}) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        allowedMentions = (allowedMentions ?: AllowedMentionsBuilder()).apply(block)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun embed(block: EmbedBuilder.() -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        embed = (embed ?: EmbedBuilder()).apply(block)
    }

    fun toMessageRequest(messageReference: Snowflake): MultipartMessageCreateRequest = MultipartMessageCreateRequest(
        MessageCreateRequest(
            content = _content,
            tts = _tts,
            embed = _embed.map { it.toRequest() },
            allowedMentions = _allowedMentions.map { it.build() },
            messageReference = Optional(
                DiscordMessageReference(
                    id = OptionalSnowflake.Value(messageReference),
                    failIfNotExists = OptionalBoolean.Value(true)
                )
            ),
            components = Optional.missingOnEmpty(components.map(MessageComponentBuilder::build))
        ),
        files
    )

    override fun toMessageRequest(): MultipartMessageCreateRequest = MultipartMessageCreateRequest(
        MessageCreateRequest(
            content = _content,
            tts = _tts,
            embed = _embed.map { it.toRequest() },
            allowedMentions = _allowedMentions.map { it.build() },
            components = Optional.missingOnEmpty(components.map(MessageComponentBuilder::build))
        ),
        files
    )

    override fun toSlashRequest(): MultipartFollowupMessageCreateRequest = MultipartFollowupMessageCreateRequest(
        FollowupMessageCreateRequest(
            content = _content,
            tts = _tts,
            embeds = if(_embed.value == null) Optional.Missing() else _embed.map { listOf(it.toRequest()) },
            allowedMentions = _allowedMentions.map { it.build() },
            components = Optional.missingOnEmpty(components.map(MessageComponentBuilder::build))
        ),
        files
    )
}