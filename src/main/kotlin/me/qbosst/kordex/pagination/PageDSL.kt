package me.qbosst.kordex.pagination

import com.kotlindiscord.kord.extensions.pagination.pages.Page
import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.EmbedBuilder.Companion.ZERO_WIDTH_SPACE
import dev.kord.rest.builder.message.EmbedBuilder.Field
import java.util.*

class PageDSL: Page("") {
    override var description: String = ""
    override var title: String? = null
    override var author: String? = null
    override var authorIcon: String? = null
    override var authorUrl: String? = null
    override var color: Color? = null
    override var footer: String? = null
    override var footerIcon: String? = null
    override var image: String? = null
    override var thumbnail: String? = null
    override var url: String? = null
    override var bundle: String? = null
    var fields: MutableList<Field> = mutableListOf()


    /**
     * Adds a new [Field] configured by the [builder].
     */
    inline fun field(builder: Field.() -> Unit) {
        fields.add(Field().apply(builder))
    }

    /**
     * Adds a new [Field] using the given [name] and [value].
     *
     * @param inline Whether the field should be rendered inline, `false` by default.
     *
     * @param value The value or 'description' of the [Field], [ZERO_WIDTH_SPACE] by default.
     * Limited to the length of [Field.Limits.value].
     *
     * @param name The name or 'title' of the [Field], [ZERO_WIDTH_SPACE] by default.
     * Limited in to the length of [Field.Limits.name].
     *
     */
    inline fun field(name: String, inline: Boolean = false, value: () -> String = { ZERO_WIDTH_SPACE }) {
        val field = Field()
        field.name = name
        field.inline = inline
        field.value = value()

        fields.add(field)
    }

    override fun build(
        locale: Locale,
        pageNum: Int,
        pages: Int,
        group: String?,
        groupIndex: Int,
        groups: Int
    ): EmbedBuilder.() -> Unit = {
        this.fields.addAll(this@PageDSL.fields)

        super.build(locale, pageNum, pages, group, groupIndex, groups).invoke(this)
    }
}