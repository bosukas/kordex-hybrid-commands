package me.qbosst.kordex.util

import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import me.qbosst.kordex.pagination.PageDSL

fun PaginatorBuilder.page(builder: PageDSL.() -> Unit) = page(PageDSL().apply(builder))

fun PaginatorBuilder.page(group: String, builder: PageDSL.() -> Unit) = page(group, PageDSL().apply(builder))