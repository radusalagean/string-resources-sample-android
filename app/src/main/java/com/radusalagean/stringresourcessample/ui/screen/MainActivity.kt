package com.radusalagean.stringresourcessample.ui.screen

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.syncSelectedLanguage()
        enableEdgeToEdge()
        setContent {
            MainScreen(viewModel)
        }
    }
}
