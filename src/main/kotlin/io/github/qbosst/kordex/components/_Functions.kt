package io.github.qbosst.kordex.components

import com.kotlindiscord.kord.extensions.components.ComponentContainer
import com.kotlindiscord.kord.extensions.components.applyComponents
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kord.rest.builder.message.modify.actionRow
import io.github.qbosst.kordex.builders.HybridMessageCreateBuilder
import io.github.qbosst.kordex.builders.HybridMessageModifyBuilder
import io.github.qbosst.kordex.builders.actionRow
import kotlin.time.Duration


/** Convenience function for applying the components in a [ComponentContainer] to a message you're creating. **/
public suspend fun HybridMessageCreateBuilder.applyComponents(components: ComponentContainer) {
    with(components) {
        sort()

        for (row in rows.filter { it.isNotEmpty() }) {
            actionRow {
                row.forEach { it.apply(this) }
            }
        }
    }
}


/** Convenience function for applying the components in a [ComponentContainer] to a message you're editing. **/
public suspend fun HybridMessageModifyBuilder.applyComponents(components: ComponentContainer) {
    with(components) {
        this@applyComponents.components = mutableListOf()

        sort()

        for (row in rows.filter { it.isNotEmpty() }) {
            actionRow {
                row.forEach { it.apply(this) }
            }
        }
    }
}


/**
 * Convenience function for creating a [ComponentContainer] and components, and applying it to a message you're
 * creating. Supply a [timeout] and the components you add will be removed from the registry after the given period
 * of inactivity.
 */
public suspend fun HybridMessageCreateBuilder.components(
    timeout: Duration? = null,
    builder: suspend ComponentContainer.() -> Unit
): ComponentContainer {
    val container = ComponentContainer(timeout, true, builder)

    applyComponents(container)

    return container
}


/**
 * Convenience function for creating a [ComponentContainer] and components, and applying it to a message you're
 * editing. Supply a [timeout] and the components you add will be removed from the registry after the given period
 * of inactivity.
 */
public suspend fun HybridMessageModifyBuilder.components(
    timeout: Duration? = null,
    builder: suspend ComponentContainer.() -> Unit
): ComponentContainer {
    val container = ComponentContainer(timeout, true, builder)

    applyComponents(container)

    return container
}
