package me.qbosst.kordex.commands.hybrid.builder

import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.delegate.delegate
import dev.kord.common.entity.optional.map
import dev.kord.common.entity.optional.mapList
import dev.kord.common.entity.optional.mapNullable
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.MessageComponentBuilder
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.json.request.FollowupMessageModifyRequest
import dev.kord.rest.json.request.MessageEditPatchRequest
import dev.kord.rest.json.request.MultipartFollowupMessageModifyRequest
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class HybridMessageModifyBuilder:
    HybridRequestBuilder<MessageEditPatchRequest, MultipartFollowupMessageModifyRequest> {

    private var _content: Optional<String> = Optional.Missing()
    var content: String? by ::_content.delegate()

    private var _embed: Optional<EmbedBuilder?> = Optional.Missing()
    var embed: EmbedBuilder? by ::_embed.delegate()

    private var _allowedMentions: Optional<AllowedMentionsBuilder> = Optional.Missing()
    var allowedMentions: AllowedMentionsBuilder? by ::_allowedMentions.delegate()

    private var _components: Optional<MutableList<MessageComponentBuilder>> = Optional.Missing()
    var components: MutableList<MessageComponentBuilder>? by ::_components.delegate()

    @OptIn(ExperimentalContracts::class)
    inline fun actionRow(builder: ActionRowBuilder.() -> Unit) {
        contract {
            callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
        }

        components = (components ?: mutableListOf()).also {
            it.add((ActionRowBuilder().apply(builder)))
        }
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

    override fun toMessageRequest(): MessageEditPatchRequest = MessageEditPatchRequest(
        content = _content,
        embed = _embed.mapNullable { it?.toRequest() },
        allowedMentions = _allowedMentions.map { it.build() },
        components = _components.mapList { it.build() }
    )

    override fun toSlashRequest(): MultipartFollowupMessageModifyRequest = MultipartFollowupMessageModifyRequest(
        FollowupMessageModifyRequest(
            content = _content,
            embeds = if(embed == null) Optional.Missing() else Optional(listOf(embed!!.toRequest())),
            allowedMentions = _allowedMentions.map { it.build() },
            components = _components.mapList { it.build() }
        )
    )
}