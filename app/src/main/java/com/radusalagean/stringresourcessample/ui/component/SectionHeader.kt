package com.radusalagean.stringresourcessample.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.radusalagean.stringresourcessample.util.string.UIText

@Composable
fun SectionHeader(
    text: UIText,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Text(
        text = text.buildString(context),
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge
    )
}