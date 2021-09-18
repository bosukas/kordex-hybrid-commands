package io.github.qbosst.kordex.pagination

import com.kotlindiscord.kord.extensions.pagination.BaseButtonPaginator
import com.kotlindiscord.kord.extensions.pagination.EXPAND_EMOJI
import com.kotlindiscord.kord.extensions.pagination.SWITCH_EMOJI
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.ReactionEmoji
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import io.github.qbosst.kordex.commands.hybrid.ephemeral.EphemeralHybridCommandContext
import io.github.qbosst.kordex.entity.EphemeralHybridMessage
import java.util.*

class EphemeralHybridFollowupPaginator(
    pages: Pages,
    owner: UserBehavior? = null,
    timeoutSeconds: Long? = null,
    keepEmbed: Boolean = true,
    switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    bundle: String? = null,
    locale: Locale? = null,
    val context: EphemeralHybridCommandContext<*>
) : BaseButtonPaginator(pages, owner, timeoutSeconds, keepEmbed, switchEmoji, bundle, locale) {
    var message: EphemeralHybridMessage? = null

    override suspend fun send() {
        if(message == null) {
            setup()

            message = context.respond {
                embed { applyPage() }

                with(this@EphemeralHybridFollowupPaginator.components) {
                    this@respond.applyToMessage()
                }
            }
        } else {
            updateButtons()

            message!!.edit {
                embed { applyPage() }

                with(this@EphemeralHybridFollowupPaginator.components) {
                    this@edit.applyToMessage()
                }
            }
        }
    }

    override suspend fun destroy() {
        if(!active) {
            return
        }

        active = false

        if(!keepEmbed) {
            if(message?.isInteraction == false) {
                message?.message?.delete()
            }
        } else {
            message?.edit {
                embed { applyPage() }

                this.components = mutableListOf()
            }
        }

        super.destroy()
    }
}

fun EphemeralHybridFollowupPaginator(
    builder: PaginatorBuilder,
    context: EphemeralHybridCommandContext<*>
): EphemeralHybridFollowupPaginator = EphemeralHybridFollowupPaginator(
    pages = builder.pages,
    owner = builder.owner,
    timeoutSeconds = builder.timeoutSeconds,
    bundle = builder.bundle,
    locale = builder.locale,
    context = context,
    switchEmoji = builder.switchEmoji ?: if (builder.pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI
)