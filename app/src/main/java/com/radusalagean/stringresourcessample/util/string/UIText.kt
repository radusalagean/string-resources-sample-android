package com.radusalagean.stringresourcessample.util.string

import android.content.Context
import android.content.res.Resources
import android.text.Spanned
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

sealed class UIText {

    protected abstract fun build(context: Context): CharSequence

    fun buildString(context: Context) = build(context) as String

    fun buildAnnotatedString(context: Context): AnnotatedString {
        return when (val charSequence = build(context)) {
            is String -> AnnotatedString(charSequence)
            is AnnotatedString -> charSequence
            else -> AnnotatedString("")
        }
    }

    private fun AnnotatedString.Builder.appendAny(arg: Any?) {
        when (arg) {
            is CharSequence -> append(arg)
            else -> append(arg.toString())
        }
    }

    protected fun resolveArg(context: Context, arg: Any?) = when (arg) {
        is UIText -> arg.build(context)
        else -> arg
    }

    protected fun resolveArgs(context: Context, args: List<Any?>?): List<Any?>? {
        args?.takeIf { it.isNotEmpty() } ?: return null
        return args.map {
            resolveArg(context, it)
        }
    }

    protected fun Resources.getStringWithPlaceholders(
        @StringRes resId: Int,
        placeholdersCount: Int
    ): String = getString(
        resId,
        *List(placeholdersCount) { "\${$it}" }.toTypedArray()
    )

    protected fun Resources.getQuantityStringWithPlaceholders(
        @PluralsRes resId: Int,
        quantity: Int,
        placeholdersCount: Int
    ): String = getQuantityString(
        resId,
        quantity,
        *List(placeholdersCount) { "\${$it}" }.toTypedArray()
    )

    protected fun buildAnnotatedString(
        context: Context,
        args: List<Pair<Any?, SpanStyle?>>,
        baseSpanStyle: SpanStyle?,
        baseStringProvider: () -> String
    ): CharSequence {
        val resolvedArgs = args.map {
            resolveArg(context, it.first) to it.second
        }
        return buildAnnotatedString {
            val baseString = baseStringProvider()
            val parts = baseString.split(placeholderRegex)
            val placeholders = placeholderRegex.findAll(baseString).map {
                it.groups[1]!!.value.toInt()
            }.toList()

            parts.forEachIndexed { index, part ->
                withStyleIfNeeded(baseSpanStyle) {
                    append(part)
                    if (index !in placeholders.indices)
                        return@withStyleIfNeeded
                    val placeholderIndex = placeholders[index]
                    val style = resolvedArgs[placeholderIndex].second
                    val arg = resolvedArgs[placeholderIndex].first
                    withStyleIfNeeded(style) {
                        appendAny(arg)
                    }
                }
            }
        }
    }

    private fun AnnotatedString.Builder.withStyleIfNeeded(
        spanStyle: SpanStyle?,
        block: () -> Unit
    ) {
        spanStyle?.let {
            withStyle(it) {
                block()
            }
        } ?: block()
    }

    data class Raw(val text: CharSequence) : UIText() {

        override fun build(context: Context): CharSequence {
            return text
        }
    }

    class Res(
        @StringRes val resId: Int,
        val args: List<Any?>
    ) : UIText() {

        constructor(
            @StringRes resId: Int,
            vararg args: Any?
        ) : this(resId, args.toList())

        override fun build(context: Context): CharSequence {
            val resolvedArgs = resolveArgs(context, args)
            return resolvedArgs?.takeIf { it.isNotEmpty() }?.let {
                context.getString(resId, *resolvedArgs.toTypedArray())
            } ?: context.getString(resId)
        }
    }

    data class PluralRes(
        @PluralsRes val resId: Int,
        val quantity: Int,
        val args: List<Any?> = listOf(quantity)
    ) : UIText() {

        constructor(
            @PluralsRes resId: Int,
            quantity: Int,
            vararg args: Any?
        ) : this(resId, quantity, args.toList())

        override fun build(context: Context): CharSequence {
            val resolvedArgs = resolveArgs(context, args) ?: emptyList()
            return context.resources.getQuantityString(resId, quantity, *resolvedArgs.toTypedArray())
        }
    }

    data class ResSpanStyle(
        @StringRes val resId: Int,
        val args: List<Pair<Any?, SpanStyle?>>,
        val baseSpanStyle: SpanStyle? = null
    ) : UIText() {

        constructor(
            @StringRes resId: Int,
            vararg args: Pair<Any?, SpanStyle?>,
            baseSpanStyle: SpanStyle? = null
        ) : this(resId, args.toList(), baseSpanStyle)

        override fun build(context: Context): CharSequence {
            return buildAnnotatedString(
                context = context,
                args = args,
                baseSpanStyle = baseSpanStyle,
                baseStringProvider = {
                    context.resources.getStringWithPlaceholders(
                        resId = resId,
                        placeholdersCount = args.size
                    )
                }
            )
        }
    }

    data class PluralResSpanStyle(
        @PluralsRes val resId: Int,
        val quantity: Int,
        val args: List<Pair<Any?, SpanStyle?>>,
        val baseSpanStyle: SpanStyle? = null
    ) : UIText() {

        constructor(
            @PluralsRes resId: Int,
            quantity: Int,
            vararg args: Pair<Any?, SpanStyle?>,
            baseSpanStyle: SpanStyle? = null
        ) : this(resId, quantity, args.toList(), baseSpanStyle)

        override fun build(context: Context): CharSequence {
            return buildAnnotatedString(
                context = context,
                args = args,
                baseSpanStyle = baseSpanStyle,
                baseStringProvider = {
                    context.resources.getQuantityStringWithPlaceholders(
                        resId = resId,
                        quantity = quantity,
                        placeholdersCount = args.size
                    )
                }
            )
        }
    }

    data class Compound(
        val components: List<UIText>
    ) : UIText() {

        constructor(vararg components: UIText) : this(components.toList())

        private fun concat(parts: List<CharSequence>): CharSequence {
            if (parts.isEmpty())
                return ""

            if (parts.size == 1)
                return parts[0]

            val styled = parts.any { it is Spanned || it is AnnotatedString }
            return if (styled) {
                buildAnnotatedString {
                    parts.forEach {
                        append(it)
                    }
                }
            } else {
                buildString {
                    parts.forEach {
                        append(it)
                    }
                }
            }
        }

        override fun build(context: Context): CharSequence {
            val resolvedComponents = components.map { it.build(context) }
            return concat(resolvedComponents)
        }
    }

    companion object {
        private val placeholderRegex = Regex("\\$\\{(\\d+)\\}")
    }
}