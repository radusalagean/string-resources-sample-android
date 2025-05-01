package com.radusalagean.stringresourcessample.ui.screen

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import com.radusalagean.stringresourcessample.R
import com.radusalagean.stringresourcessample.ui.component.ExampleEntryModel
import com.radusalagean.stringresourcessample.ui.component.LanguageOption
import com.radusalagean.stringresourcessample.ui.theme.CustomGreen
import com.radusalagean.stringresourcessample.util.string.UIText
import com.radusalagean.stringresourcessample.util.string.uiTextAnnotationList
import com.radusalagean.stringresourcessample.util.string.uiTextBuilder

class MainViewModel : ViewModel() {

    // Section: Language
    val languageSectionTitle = UIText.Res(R.string.section_title_language)
    val languageOptions = listOf(
        LanguageOption(
            uiText = UIText.Res(R.string.language_english),
            languageCode = "en"
        ),
        LanguageOption(
            uiText = UIText.Res(R.string.language_romanian),
            languageCode = "ro"
        )
    )
    var selectedLanguageCode by mutableStateOf("")
    val selectedLanguageIndex: Int by derivedStateOf {
        languageOptions.indexOfFirst { it.languageCode == selectedLanguageCode }
    }

    fun syncSelectedLanguage() {
        val locales = AppCompatDelegate.getApplicationLocales()
        selectedLanguageCode = locales.get(0)?.language ?: "en"
    }

    fun onLanguageSelected(code: String) {
        val localesList = LocaleListCompat.forLanguageTags(code)
        AppCompatDelegate.setApplicationLocales(localesList)
    }

    // Section: Examples
    val examplesSectionTitle = UIText.Res(R.string.section_title_examples)
    val exampleEntries = listOf(
        ExampleEntryModel(
            label = UIText.Raw("UIText.Raw"),
            value = UIText.Raw("Radu")
        ),
        ExampleEntryModel(
            label = UIText.Raw("UIText.Res"),
            value = UIText.Res(R.string.greeting, "Radu")
        ),
        ExampleEntryModel(
            label = UIText.Raw("UIText.PluralRes"),
            value = UIText.PluralRes(R.plurals.products, 30)
        ),
        ExampleEntryModel(
            label = UIText.Raw("UIText.ResAnnotated"),
            value = UIText.ResAnnotated(
                R.string.shopping_cart_status,
                UIText.PluralRes(
                    R.plurals.products,
                    30
                ) to null,
                UIText.Res(
                    R.string.shopping_cart_status_insert_shopping_cart
                ) to SpanStyle(color = Color.Red).uiTextAnnotationList()
            )
        ),
        ExampleEntryModel(
            label = UIText.Raw("UIText.PluralResAnnotated"),
            value = UIText.PluralResAnnotated(
                R.plurals.products,
                quantity = 30,
                30 to SpanStyle(
                    color = CustomGreen
                ).uiTextAnnotationList(),
                baseAnnotations = SpanStyle(
                    fontWeight = FontWeight.Bold
                ).uiTextAnnotationList()
            )
        ),
        ExampleEntryModel(
            label = UIText.Raw("UIText.Compound"),
            value = UIText.Compound(
                UIText.Res(R.string.greeting, "Radu"),
                UIText.Raw(" "),
                UIText.ResAnnotated(
                    R.string.shopping_cart_status,
                    UIText.PluralResAnnotated(
                        R.plurals.products,
                        quantity = 30,
                        30 to SpanStyle(
                            color = CustomGreen
                        ).uiTextAnnotationList(),
                        baseAnnotations = SpanStyle(
                            fontWeight = FontWeight.Bold,
                        ).uiTextAnnotationList()
                    ) to null,
                    UIText.Res(
                        R.string.shopping_cart_status_insert_shopping_cart
                    ) to SpanStyle(color = Color.Red).uiTextAnnotationList()
                )
            )
        ),
        ExampleEntryModel(
            label = UIText.Raw("DSL Builder - Example 1"),
            value = uiTextBuilder {
                res(R.string.greeting) {
                    arg("Radu")
                }
                raw(" ")
                resAnnotated(R.string.shopping_cart_status) {
                    arg(
                        uiTextBuilder {
                            pluralResAnnotated(
                                R.plurals.products,
                                quantity = 30,
                                baseSpanStyle = SpanStyle(fontWeight = FontWeight.Bold)
                            ) {
                                arg(
                                    30.toString(),
                                    SpanStyle(color = CustomGreen)
                                )
                            }
                        }
                    )
                    arg(
                        uiTextBuilder {
                            res(R.string.shopping_cart_status_insert_shopping_cart)
                        },
                        SpanStyle(color = Color.Red)
                    )
                }
                raw(" ")
                resAnnotated(
                    resId = R.string.proceed_to_checkout,
                    baseLinkAnnotation = LinkAnnotation.Url(
                        url = "https://example.com",
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = Color.Blue,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                )
            }
        ),
        ExampleEntryModel(
            label = UIText.Raw("DSL Builder - Example 2"),
            value = uiTextBuilder {
                res(R.string.greeting) {
                    arg("Radu")
                }
                resAnnotated(R.string.shopping_cart_status, {
                    paragraph(ParagraphStyle())
                }) {
                    arg(
                        uiTextBuilder {
                            pluralResAnnotated(
                                R.plurals.products,
                                quantity = 30,
                                baseSpanStyle = SpanStyle(fontWeight = FontWeight.Bold)
                            ) {
                                arg(
                                    30.toString(),
                                    SpanStyle(color = CustomGreen)
                                )
                            }
                        }
                    )
                    arg(
                        uiTextBuilder {
                            res(R.string.shopping_cart_status_insert_shopping_cart)
                        },
                        SpanStyle(color = Color.Red)
                    )
                }
                resAnnotated(
                    resId = R.string.proceed_to_checkout,
                    baseLinkAnnotation = LinkAnnotation.Url(
                        url = "https://example.com",
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = Color.Blue,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                )
            }
        )
    )
}