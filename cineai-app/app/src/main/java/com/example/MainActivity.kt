package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.repository.AppContainer
import com.example.ui.navigation.CineAppShell
import com.example.ui.theme.CineStudioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.initialize(this)
        enableEdgeToEdge()
        setContent {
            CineStudioTheme {
                CineAppShell()
            }
        }
    }
}

