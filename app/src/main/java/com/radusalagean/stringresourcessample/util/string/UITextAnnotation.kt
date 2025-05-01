package com.radusalagean.stringresourcessample.util.string

import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle

sealed interface UITextAnnotation {

    @JvmInline
    value class Span(
        val spanStyle: SpanStyle
    ) : UITextAnnotation

    @JvmInline
    value class Paragraph(
        val paragraphStyle: ParagraphStyle
    ) : UITextAnnotation

    @JvmInline
    value class Link(
        val linkAnnotation: LinkAnnotation
    ) : UITextAnnotation
}

fun SpanStyle.uiTextAnnotation() = UITextAnnotation.Span(this)
fun SpanStyle.uiTextAnnotationList() = listOf(UITextAnnotation.Span(this))
fun ParagraphStyle.uiTextAnnotation() = UITextAnnotation.Paragraph(this)
fun ParagraphStyle.uiTextAnnotationList() = listOf(UITextAnnotation.Paragraph(this))
fun LinkAnnotation.uiTextAnnotation() = UITextAnnotation.Link(this)
fun LinkAnnotation.uiTextAnnotationList() = listOf(UITextAnnotation.Link(this))