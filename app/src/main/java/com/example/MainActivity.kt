package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.theme.PerennialTheme
import com.example.ui.theme.ThemeViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
  private val themeViewModel: ThemeViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
      PerennialTheme(darkTheme = isDarkTheme) {
        PerennialApp(themeViewModel = themeViewModel)
      }
    }
  }
}

