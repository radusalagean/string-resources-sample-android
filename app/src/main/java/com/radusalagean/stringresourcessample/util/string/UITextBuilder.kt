package com.radusalagean.stringresourcessample.util.string

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle

@DslMarker
annotation class UITextDslMarker

fun uiTextBuilder(block: UITextBuilder.() -> Unit): UIText = UITextBuilder().apply(block).build()

@UITextDslMarker
class UITextBuilder {
    private val components = mutableListOf<UIText>()

    fun raw(text: CharSequence) {
        components += UIText.Raw(text)
    }

    fun res(
        @StringRes resId: Int,
        block: ArgsBuilder.() -> Unit = { }
    ) {
        val args = ArgsBuilder().apply(block).build()
        components += UIText.Res(resId, *args.toTypedArray())
    }

    fun pluralRes(
        @PluralsRes resId: Int,
        quantity: Int,
        block: ArgsBuilder.() -> Unit = {
            arg(quantity.toString())
        }
    ) {
        val args = ArgsBuilder().apply(block).build()
        components += UIText.PluralRes(resId, quantity, *args.toTypedArray())
    }

    fun resAnnotated(
        @StringRes resId: Int,
        baseAnnotation: UITextAnnotation? = null,
        block: AnnotatedArgsBuilder.() -> Unit = { }
    ) {
        val annotatedArgs = AnnotatedArgsBuilder().apply(block).build()
        components += UIText.ResAnnotated(resId, annotatedArgs, baseAnnotation)
    }

    fun resAnnotated(
        @StringRes resId: Int,
        block: AnnotatedArgsBuilder.() -> Unit = { }
    ) = resAnnotated(
        resId = resId,
        baseAnnotation = null,
        block = block
    )

    fun resAnnotated(
        @StringRes resId: Int,
        baseSpanStyle: SpanStyle,
        block: AnnotatedArgsBuilder.() -> Unit = { }
    ) = resAnnotated(
        resId = resId,
        baseAnnotation = baseSpanStyle.uiTextAnnotation(),
        block = block
    )

    fun resAnnotated(
        @StringRes resId: Int,
        baseParagraphStyle: ParagraphStyle,
        block: AnnotatedArgsBuilder.() -> Unit = { }
    ) = resAnnotated(
        resId = resId,
        baseAnnotation = baseParagraphStyle.uiTextAnnotation(),
        block = block
    )

    fun resAnnotated(
        @StringRes resId: Int,
        baseLinkAnnotation: LinkAnnotation,
        block: AnnotatedArgsBuilder.() -> Unit = { }
    ) = resAnnotated(
        resId = resId,
        baseAnnotation = baseLinkAnnotation.uiTextAnnotation(),
        block = block
    )

    fun pluralResAnnotated(
        @PluralsRes resId: Int,
        quantity: Int,
        baseAnnotation: UITextAnnotation? = null,
        block: AnnotatedArgsBuilder.() -> Unit = {
            arg(quantity.toString())
        }
    ) {
        val annotatedArgs = AnnotatedArgsBuilder().apply(block).build()
        components += UIText.PluralResAnnotated(resId, quantity, annotatedArgs, baseAnnotation)
    }

    fun pluralResAnnotated(
        @PluralsRes resId: Int,
        quantity: Int,
        block: AnnotatedArgsBuilder.() -> Unit = {
            arg(quantity.toString())
        }
    ) = pluralResAnnotated(
        resId = resId,
        quantity = quantity,
        baseAnnotation = null,
        block = block
    )

    fun pluralResAnnotated(
        @PluralsRes resId: Int,
        quantity: Int,
        baseSpanStyle: SpanStyle,
        block: AnnotatedArgsBuilder.() -> Unit = {
            arg(quantity.toString())
        }
    ) = pluralResAnnotated(
        resId = resId,
        quantity = quantity,
        baseAnnotation = baseSpanStyle.uiTextAnnotation(),
        block = block
    )

    fun pluralResAnnotated(
        @PluralsRes resId: Int,
        quantity: Int,
        baseParagraphStyle: ParagraphStyle,
        block: AnnotatedArgsBuilder.() -> Unit = {
            arg(quantity.toString())
        }
    ) = pluralResAnnotated(
        resId = resId,
        quantity = quantity,
        baseAnnotation = baseParagraphStyle.uiTextAnnotation(),
        block = block
    )

    fun pluralResAnnotated(
        @PluralsRes resId: Int,
        quantity: Int,
        baseLinkAnnotation: LinkAnnotation,
        block: AnnotatedArgsBuilder.() -> Unit = {
            arg(quantity.toString())
        }
    ) = pluralResAnnotated(
        resId = resId,
        quantity = quantity,
        baseAnnotation = baseLinkAnnotation.uiTextAnnotation(),
        block = block
    )

    internal fun build(): UIText = when (components.size) {
        0 -> UIText.Raw("")
        1 -> components[0]
        else -> UIText.Compound(components)
    }
}

@UITextDslMarker
class ArgsBuilder {
    private val args = mutableListOf<Any?>()

    fun arg(arg: CharSequence) {
        args += arg
    }

    fun arg(arg: UIText) {
        args += arg
    }

    fun build(): List<Any?> = args
}

@UITextDslMarker
class AnnotatedArgsBuilder {

    private val args = mutableListOf<Pair<Any?, UITextAnnotation?>>()

    fun arg(value: CharSequence?) {
        args += value to null
    }

    fun arg(value: CharSequence?, annotation: UITextAnnotation? = null) {
        args += value to annotation
    }

    fun arg(value: CharSequence?, spanStyle: SpanStyle) =
        arg(value, spanStyle.uiTextAnnotation())

    fun arg(value: CharSequence?, paragraphStyle: ParagraphStyle) =
        arg(value, paragraphStyle.uiTextAnnotation())

    fun arg(value: CharSequence?, linkAnnotation: LinkAnnotation) =
        arg(value, linkAnnotation.uiTextAnnotation())

    fun arg(value: UIText?) {
        args += value to null
    }

    fun arg(value: UIText?, annotation: UITextAnnotation? = null) {
        args += value to annotation
    }

    fun arg(value: UIText?, spanStyle: SpanStyle) =
        arg(value, spanStyle.uiTextAnnotation())

    fun arg(value: UIText?, paragraphStyle: ParagraphStyle) =
        arg(value, paragraphStyle.uiTextAnnotation())

    fun arg(value: UIText?, linkAnnotation: LinkAnnotation) =
        arg(value, linkAnnotation.uiTextAnnotation())

    fun build(): List<Pair<Any?, UITextAnnotation?>> = args
}
