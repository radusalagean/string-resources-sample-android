package com.radusalagean.stringresourcessample.util.string

import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle

sealed interface UITextAnnotation {

    data class SpanStyle(
        val spanStyle: androidx.compose.ui.text.SpanStyle
    ) : UITextAnnotation

    data class ParagraphStyle(
        val paragraphStyle: androidx.compose.ui.text.ParagraphStyle
    ) : UITextAnnotation

    data class LinkAnnotation(
        val linkAnnotation: androidx.compose.ui.text.LinkAnnotation
    ) : UITextAnnotation
}

fun SpanStyle.uiTextAnnotation() = UITextAnnotation.SpanStyle(this)
fun ParagraphStyle.uiTextAnnotation() = UITextAnnotation.ParagraphStyle(this)
fun LinkAnnotation.uiTextAnnotation() = UITextAnnotation.LinkAnnotation(this)