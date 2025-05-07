package com.radusalagean.stringresourcessample.util.string

import android.content.Context
import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle

sealed class UIText {

    protected abstract fun build(context: Context): CharSequence

    fun buildString(context: Context): String {
        return when (val charSequence = build(context)) {
            is String -> charSequence
            is AnnotatedString -> charSequence.toString() // We drop any style here
            else -> ""
        }
    }

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

    /**
     * In order to work around the default behavior of Android's getString(...) which will
     *  drop any associated style, we generate placeholders in the form of ${0}, ${1}, etc
     *  which will allow us to apply our custom logic to inject the arguments without losing
     *  associated styles, in the correct order.
     *
     * Warning: We use ${digit} placeholders, so make sure you don't have such patterns hardcoded in
     *  your string resources. Also, make sure you exclusively use "%s" (no order needed) or
     *  "%1$s", "%2$s", etc. (specified order) in string files for arguments.
     *
     * Other formats in string resources "%.2f", "%d", etc. are not supported, but you can safely
     *  replace them with string format arguments (e.g. %s) and offload formatting of other types
     *  to your kotlin code.
     */
    private fun generatePlaceholderArgs(placeholdersCount: Int) =
        List(placeholdersCount) { "\${$it}" }.toTypedArray()


    protected fun Resources.getStringWithPlaceholders(
        @StringRes resId: Int,
        placeholdersCount: Int
    ): String = getString(
        resId,
        *generatePlaceholderArgs(placeholdersCount)
    )

    protected fun Resources.getQuantityStringWithPlaceholders(
        @PluralsRes resId: Int,
        quantity: Int,
        placeholdersCount: Int
    ): String = getQuantityString(
        resId,
        quantity,
        *generatePlaceholderArgs(placeholdersCount)
    )

    protected fun buildAnnotatedString(
        resolvedArgs: List<Pair<Any?, List<UITextAnnotation>>>,
        baseAnnotations: List<UITextAnnotation>,
        baseStringProvider: () -> String
    ): CharSequence {
        return buildAnnotatedString {
            val baseString = baseStringProvider()
            val parts = baseString.split(placeholderRegex)
            val placeholders = placeholderRegex.findAll(baseString).map {
                it.groups[1]!!.value.toInt()
            }.toList()

            handleUITextAnnotations(baseAnnotations) {
                parts.forEachIndexed { index, part ->
                    append(part)
                    if (index !in placeholders.indices)
                        return@handleUITextAnnotations
                    val placeholderIndex = placeholders[index]
                    val uiTextAnnotations = resolvedArgs[placeholderIndex].second
                    val arg = resolvedArgs[placeholderIndex].first
                    handleUITextAnnotations(uiTextAnnotations) {
                        appendAny(arg)
                    }
                }
            }
        }
    }

    private fun AnnotatedString.Builder.handleUITextAnnotations(
        uiTextAnnotations: List<UITextAnnotation>,
        block: () -> Unit
    ) {

        if (uiTextAnnotations.isEmpty()) {
            block()
            return
        }

        fun applyAnnotation(index: Int) {
            if (index >= uiTextAnnotations.size) {
                block()
                return
            }

            when (val annotation = uiTextAnnotations[index]) {
                is UITextAnnotation.Span -> {
                    withStyle(annotation.spanStyle) {
                        applyAnnotation(index + 1)
                    }
                }
                is UITextAnnotation.Paragraph -> {
                    withStyle(annotation.paragraphStyle) {
                        applyAnnotation(index + 1)
                    }
                }
                is UITextAnnotation.Link -> {
                    withLink(annotation.linkAnnotation) {
                        applyAnnotation(index + 1)
                    }
                }
            }
        }

        applyAnnotation(0)
    }

    data class Raw(val text: CharSequence) : UIText() {

        override fun build(context: Context): CharSequence {
            return text
        }
    }

    data class Res(
        @StringRes val resId: Int,
        val args: List<Pair<Any?, List<UITextAnnotation>>>,
        val baseAnnotations: List<UITextAnnotation>
    ) : UIText() {

        override fun build(context: Context): CharSequence {
            val resolvedArgs = args.map {
                resolveArg(context, it.first) to it.second
            }
            val annotated = baseAnnotations.isNotEmpty() ||
                    args.any { it.second.isNotEmpty() } ||
                    resolvedArgs.any { it.first is AnnotatedString }
            return if (annotated) {
                buildAnnotatedString(
                    resolvedArgs = resolvedArgs,
                    baseAnnotations = baseAnnotations,
                    baseStringProvider = {
                        context.resources.getStringWithPlaceholders(
                            resId = resId,
                            placeholdersCount = args.size
                        )
                    }
                )
            } else if (resolvedArgs.isNotEmpty()) {
                context.getString(resId, *resolvedArgs.map { it.first }.toTypedArray())
            } else {
                context.getString(resId)
            }
        }
    }

    data class PluralRes(
        @PluralsRes val resId: Int,
        val quantity: Int,
        val args: List<Pair<Any?, List<UITextAnnotation>>>,
        val baseAnnotations: List<UITextAnnotation>
    ) : UIText() {

        override fun build(context: Context): CharSequence {
            val resolvedArgs = args.map {
                resolveArg(context, it.first) to it.second
            }
            val annotated = baseAnnotations.isNotEmpty() ||
                    args.any { it.second.isNotEmpty() } ||
                    resolvedArgs.any { it.first is AnnotatedString }

            return if (annotated) {
                buildAnnotatedString(
                    resolvedArgs = resolvedArgs,
                    baseAnnotations = baseAnnotations,
                    baseStringProvider = {
                        context.resources.getQuantityStringWithPlaceholders(
                            resId = resId,
                            quantity = quantity,
                            placeholdersCount = args.size
                        )
                    }
                )
            } else if (resolvedArgs.isNotEmpty()) {
                context.resources.getQuantityString(resId, quantity,
                    *resolvedArgs.map { it.first }.toTypedArray())
            } else {
                context.resources.getQuantityString(resId, quantity)
            }
        }
    }

    data class Compound(
        val components: List<UIText>
    ) : UIText() {

        private fun concat(parts: List<CharSequence>): CharSequence {
            if (parts.isEmpty())
                return ""

            if (parts.size == 1)
                return parts[0]

            val annotated = parts.any { it is AnnotatedString }
            return if (annotated) {
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