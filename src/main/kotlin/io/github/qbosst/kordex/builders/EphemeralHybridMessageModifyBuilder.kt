package io.github.qbosst.kordex.builders

import dev.kord.common.entity.MessageFlags
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.delegate.delegate
import dev.kord.common.entity.optional.map
import dev.kord.common.entity.optional.mapList
import dev.kord.rest.builder.component.MessageComponentBuilder
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.EphemeralMessageModifyBuilder
import dev.kord.rest.json.request.*

class EphemeralHybridMessageModifyBuilder : EphemeralMessageModifyBuilder {
    private var _content: Optional<String> = Optional.Missing()
    override var content: String? by ::_content.delegate()

    private var _embeds: Optional<MutableList<EmbedBuilder>> = Optional.Missing()
    override var embeds: MutableList<EmbedBuilder>? by ::_embeds.delegate()

    private var _allowedMentions: Optional<AllowedMentionsBuilder> = Optional.Missing()
    override var allowedMentions: AllowedMentionsBuilder? by ::_allowedMentions.delegate()

    private var _components: Optional<MutableList<MessageComponentBuilder>> = Optional.Missing()
    override var components: MutableList<MessageComponentBuilder>? by ::_components.delegate()

    fun toMessagePatchRequest(flags: MessageFlags? = null) = MultipartMessagePatchRequest(
        MessageEditPatchRequest(
            content = _content,
            embeds = _embeds.mapList { it.toRequest() },
            flags = Optional(flags),
            allowedMentions = _allowedMentions.map { it.build() },
            components = _components.mapList { it.build() }
        )
    )

    fun toFollowupMessageModifyRequest() = MultipartFollowupMessageModifyRequest(
        FollowupMessageModifyRequest(
            content = _content,
            embeds = _embeds.mapList { it.toRequest() },
            allowedMentions = _allowedMentions.map { it.build() },
            components = _components.mapList { it.build() }
        )
    )
}