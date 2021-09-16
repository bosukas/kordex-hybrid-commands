package io.github.qbosst.kordex.commands.hybrid

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommandContext
import com.kotlindiscord.kord.extensions.commands.chat.ChatCommandContext
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.sentry.SentryContext
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Message
import dev.kord.core.event.Event
import io.github.qbosst.kordex.entity.HybridMessage
import java.util.*

abstract class HybridCommandContext<C : HybridCommandContext<C, A>, A : Arguments>(val context: CommandContext) {
    init {
        if(context !is SlashCommandContext<*, *> && context !is ChatCommandContext<*>) {
            error("Unknown context type provided.")
        }
    }

    /** Respective command for this context object. **/
    val command: Command get() = context.command

    /** Event that triggered this command. **/
    val eventObj: Event get() = context.eventObj

    /** Translations provider, for retrieving translations. **/
    val translationsProvider: TranslationsProvider get() = context.translationsProvider

    /** Current Sentry context, containing breadcrumbs and other goodies. **/
    val sentry: SentryContext get() = context.sentry

    val kord: Kord get() = eventObj.kord

    @Suppress("UNCHECKED_CAST")
    val slashContext: SlashCommandContext<*, A> get() = context as SlashCommandContext<*, A>

    @Suppress("UNCHECKED_CAST")
    val chatContext: ChatCommandContext<A> get() = context as ChatCommandContext<A>

    val isSlashContext: Boolean get() = context is SlashCommandContext<*, *>

    val isChatContext: Boolean get() = context is ChatCommandContext<*>

    /** Resolve the locale for this command context. **/
    suspend fun getLocale(): Locale = context.getLocale()

    /**
     * Given a translation key and bundle name, return the translation for the locale provided by the bot's configured
     * locale resolvers.
     */
    suspend fun translate(
        key: String,
        bundleName: String?,
        replacements: Array<Any?> = arrayOf()
    ): String = context.translate(key, bundleName, replacements)

    /**
     * Given a translation key and possible replacements,return the translation for the given locale in the
     * extension's configured bundle, for the locale provided by the bot's configured locale resolvers.
     */
    suspend fun translate(key: String, replacements: Array<Any?> = arrayOf()): String = context.translate(
        key,
        command.extension.bundle,
        replacements
    )

    /** Channel this command was executed within. **/
    val channel: MessageChannelBehavior get() = if(isSlashContext) slashContext.channel else chatContext.channel

    /** Guild this command was executed within, if any. **/
    val guild: GuildBehavior? get() = if(isSlashContext) slashContext.guild else chatContext.guild

    /** Member that executed this command, if on a guild. **/
    val member: MemberBehavior? get() = if(isSlashContext) slashContext.member else chatContext.member

    /** User that executed this command. **/
    val user: UserBehavior? get() = if(isSlashContext) slashContext.user else chatContext.user

    /** Message object containing this command invocation. **/
    val message: Message? get() = if(isSlashContext) null else chatContext.message

    /** Object representing this slash command's arguments, if any. **/
    val arguments: A get() = if(isSlashContext) slashContext.arguments else chatContext.arguments
}