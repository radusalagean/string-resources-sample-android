package com.radusalagean.stringresourcessample.util.string

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle

@DslMarker
annotation class UITextDslMarker

fun UIText(block: UITextBuilder.() -> Unit): UIText = UITextBuilder().apply(block).build()

@UITextDslMarker
class UITextBuilder {
    private val components = mutableListOf<UIText>()

    fun raw(text: CharSequence) {
        components += UIText.Raw(text)
    }

    fun res(
        @StringRes resId: Int,
        resBuilder: ResBuilder.() -> Unit = { }
    ) {
        val config = ResBuilder().apply(resBuilder).buildResConfig()
        components += UIText.Res(
            resId = resId,
            args = config.args,
            baseAnnotations = config.annotations
        )
    }

    fun pluralRes(
        @PluralsRes resId: Int,
        quantity: Int,
        resBuilder: ResBuilder.() -> Unit = {
            arg(quantity.toString())
        }
    ) {
        val config = ResBuilder().apply(resBuilder).buildResConfig()
        components += UIText.PluralRes(
            resId = resId,
            quantity = quantity,
            args = config.args,
            baseAnnotations = config.annotations
        )
    }

    internal fun build(): UIText = when (components.size) {
        0 -> UIText.Raw("")
        1 -> components[0]
        else -> UIText.Compound(components)
    }
}

@UITextDslMarker
class ResBuilder : AnnotationsBuilder() {
    private val args = mutableListOf<Pair<Any?, List<UITextAnnotation>>>()

    fun arg(value: CharSequence, annotationsBuilder: AnnotationsBuilder.() -> Unit = { }) {
        val annotations = AnnotationsBuilder().apply(annotationsBuilder).buildAnnotations()
        args += value to annotations
    }

    fun arg(value: UIText, annotationsBuilder: AnnotationsBuilder.() -> Unit = { }) {
        val annotations = AnnotationsBuilder().apply(annotationsBuilder).buildAnnotations()
        args += value to annotations
    }

    fun buildResConfig(): ResAnnotatedConfig = ResAnnotatedConfig(
        annotations = annotations,
        args = args
    )
}

data class ResAnnotatedConfig(
    val annotations: List<UITextAnnotation>,
    val args: List<Pair<Any?, List<UITextAnnotation>>>
)

@UITextDslMarker
open class AnnotationsBuilder {
    protected val annotations = mutableListOf<UITextAnnotation>()

    operator fun SpanStyle.unaryPlus() {
        annotations.add(UITextAnnotation.Span(this))
    }

    operator fun ParagraphStyle.unaryPlus() {
        annotations.add(UITextAnnotation.Paragraph(this))
    }

    operator fun LinkAnnotation.unaryPlus() {
        annotations.add(UITextAnnotation.Link(this))
    }

    fun buildAnnotations(): List<UITextAnnotation> = annotations
}