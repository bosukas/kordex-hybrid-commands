package me.qbosst.kordex.commands.hybrid.builder

import dev.kord.common.entity.MessageFlags
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.delegate.delegate
import dev.kord.common.entity.optional.map
import dev.kord.common.entity.optional.mapList
import dev.kord.rest.builder.component.MessageComponentBuilder
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.PersistentMessageModifyBuilder
import dev.kord.rest.json.request.FollowupMessageModifyRequest
import dev.kord.rest.json.request.MessageEditPatchRequest
import dev.kord.rest.json.request.MultipartFollowupMessageModifyRequest
import dev.kord.rest.json.request.MultipartMessagePatchRequest
import java.io.InputStream

class HybridMessageModifyBuilder : PersistentMessageModifyBuilder,
    HybridRequestBuilder<MultipartMessagePatchRequest, MultipartFollowupMessageModifyRequest> {

    private var _content: Optional<String> = Optional.Missing()
    override var content: String? by ::_content.delegate()

    private var _embeds: Optional<MutableList<EmbedBuilder>> = Optional.Missing()
    override var embeds: MutableList<EmbedBuilder>? by ::_embeds.delegate()

    private var _allowedMentions: Optional<AllowedMentionsBuilder> = Optional.Missing()
    override var allowedMentions: AllowedMentionsBuilder? by ::_allowedMentions.delegate()

    private var _components: Optional<MutableList<MessageComponentBuilder>> = Optional.Missing()
    override var components: MutableList<MessageComponentBuilder>? by ::_components.delegate()

    private var _files: Optional<MutableList<Pair<String, InputStream>>> = Optional.Missing()
    override var files: MutableList<Pair<String, InputStream>>? by ::_files.delegate()

    override fun toMessageRequest(): MultipartMessagePatchRequest = toMessageRequest(null)

    fun toMessageRequest(flags: MessageFlags? = null): MultipartMessagePatchRequest {
        return MultipartMessagePatchRequest(
            MessageEditPatchRequest(
                content = _content,
                embeds = _embeds.mapList { it.toRequest() },
                flags = Optional(flags),
                allowedMentions = _allowedMentions.map { it.build() },
                components = _components.mapList { it.build() }
            ),
            _files
        )
    }

    override fun toSlashRequest(): MultipartFollowupMessageModifyRequest {
        return MultipartFollowupMessageModifyRequest(
            FollowupMessageModifyRequest(
                content = _content,
                embeds = _embeds.mapList { it.toRequest() },
                allowedMentions = _allowedMentions.map { it.build() },
                components = _components.mapList { it.build() }
            ),
            _files
        )
    }
}