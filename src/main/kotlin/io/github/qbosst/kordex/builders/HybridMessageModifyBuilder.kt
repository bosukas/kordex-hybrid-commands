package io.github.qbosst.kordex.builders

import dev.kord.common.entity.DiscordAttachment
import dev.kord.common.entity.MessageFlags
import dev.kord.rest.NamedFile
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.MessageComponentBuilder
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import dev.kord.common.entity.optional.delegate.delegate
import dev.kord.common.entity.optional.map
import dev.kord.common.entity.optional.mapList
import dev.kord.rest.json.request.FollowupMessageModifyRequest
import dev.kord.rest.json.request.MessageEditPatchRequest
import dev.kord.rest.json.request.MultipartFollowupMessageModifyRequest
import dev.kord.rest.json.request.MultipartMessagePatchRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

class HybridMessageModifyBuilder {
    private var state = MessageModifyStateHolder()

    var files: MutableList<NamedFile>? by state::files.delegate()

    var attachments: MutableList<DiscordAttachment>? by state::attachments.delegate()

    var content: String? by state::content.delegate()

    var embeds: MutableList<EmbedBuilder>? by state::embeds.delegate()

    var allowedMentions: AllowedMentionsBuilder? by state::allowedMentions.delegate()

    var components: MutableList<MessageComponentBuilder>? by state::components.delegate()

    public fun addFile(name: String, content: InputStream): NamedFile {
        val namedFile = NamedFile(name, content)

        files = (files ?: mutableListOf()).also {
            it.add(namedFile)
        }

        return namedFile
    }

    public suspend fun addFile(path: Path): NamedFile = withContext(Dispatchers.IO) {
        addFile(path.fileName.toString(), Files.newInputStream(path))
    }

    fun toFollowupRequest(): MultipartFollowupMessageModifyRequest {
        return MultipartFollowupMessageModifyRequest(
            FollowupMessageModifyRequest(
                content = state.content,
                embeds = state.embeds.mapList { it.toRequest() },
                allowedMentions = state.allowedMentions.map { it.build() },
                components = state.components.mapList { it.build() },
                attachments = state.attachments
            ),
            state.files
        )
    }

    fun toChatRequest(flags: MessageFlags? = null): MultipartMessagePatchRequest = MultipartMessagePatchRequest(
        MessageEditPatchRequest(
            content = state.content,
            embeds = state.embeds.mapList { it.toRequest() },
            flags = state.flags,
            allowedMentions = state.allowedMentions.map { it.build() },
            components = state.components.mapList { it.build() },
            attachments = state.attachments
        ),
        state.files,
    )
}

@OptIn(ExperimentalContracts::class)
public inline fun HybridMessageModifyBuilder.embed(block: EmbedBuilder.() -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    embeds = (embeds ?: mutableListOf()).also {
        it.add(EmbedBuilder().apply(block))
    }
}

/**
 * Configures the mentions that should trigger a ping. Not calling this function will result in the default behavior
 * (ping everything), calling this function but not configuring it before the request is build will result in all
 * pings being ignored.
 */
@OptIn(ExperimentalContracts::class)
public inline fun HybridMessageModifyBuilder.allowedMentions(block: AllowedMentionsBuilder.() -> Unit = {}) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    allowedMentions = (allowedMentions ?: AllowedMentionsBuilder()).apply(block)
}


@OptIn(ExperimentalContracts::class)
public inline fun HybridMessageModifyBuilder.actionRow(builder: ActionRowBuilder.() -> Unit) {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
    components = (components ?: mutableListOf()).also {
        it.add(ActionRowBuilder().apply(builder))
    }
}