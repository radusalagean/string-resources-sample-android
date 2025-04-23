package com.radusalagean.stringresourcessample.util.string

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.ui.text.SpanStyle

@DslMarker
annotation class UITextDsl

fun uiTextBuilder(block: UITextBuilder.() -> Unit): UIText = UITextBuilder().apply(block).build()

@UITextDsl
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

    fun resSpanStyle(
        @StringRes resId: Int,
        baseStyle: SpanStyle? = null,
        block: StyledArgsBuilder.() -> Unit
    ) {
        val styledArgs = StyledArgsBuilder().apply(block).build()
        components += UIText.ResSpanStyle(resId, styledArgs, baseStyle)
    }

    fun pluralResSpanStyle(
        @PluralsRes resId: Int,
        quantity: Int,
        baseStyle: SpanStyle? = null,
        block: StyledArgsBuilder.() -> Unit
    ) {
        val styledArgs = StyledArgsBuilder().apply(block).build()
        components += UIText.PluralResSpanStyle(resId, quantity, styledArgs, baseStyle)
    }

    internal fun build(): UIText = when (components.size) {
        0 -> UIText.Raw("")
        1 -> components[0]
        else -> UIText.Compound(components)
    }
}

@UITextDsl
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

@UITextDsl
class StyledArgsBuilder {

    private val args = mutableListOf<Pair<Any?, SpanStyle?>>()

    fun arg(value: CharSequence?, style: SpanStyle? = null) {
        args += value to style
    }

    fun arg(value: UIText?, style: SpanStyle? = null) {
        args += value to style
    }

    fun build(): List<Pair<Any?, SpanStyle?>> = args
}
