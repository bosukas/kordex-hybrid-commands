package me.qbosst.kordex.commands.hybrid.builder

interface HybridRequestBuilder<M, S> {
    fun toMessageRequest(): M

    fun toSlashRequest(): S
}