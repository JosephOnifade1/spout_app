package com.example

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.theme.PerennialTheme
import com.example.ui.theme.ThemeViewModel
import com.google.firebase.FirebaseApp
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
  private val themeViewModel: ThemeViewModel by viewModels()
  private var tts: TextToSpeech? = null
  private var textToSpeak: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Check if we were launched from a voice reminder
    textToSpeak = intent?.getStringExtra("speak_text")
    if (textToSpeak != null) {
      initTextToSpeech()
    }

    setContent {
      val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
      PerennialTheme(darkTheme = isDarkTheme) {
        PerennialApp(themeViewModel = themeViewModel)
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    val speak = intent.getStringExtra("speak_text")
    if (speak != null) {
      textToSpeak = speak
      initTextToSpeech()
    }
  }

  private fun initTextToSpeech() {
    if (tts == null) {
      tts = TextToSpeech(this, this)
    } else {
      speak()
    }
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      val result = tts?.setLanguage(Locale.US)
      if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        Log.e("MainActivity", "TTS Language is not supported!")
      } else {
        speak()
      }
    } else {
      Log.e("MainActivity", "TTS Initialization failed!")
    }
  }

  private fun speak() {
    val text = textToSpeak ?: return
    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "HabitReminderTTS")
    textToSpeak = null // consume it
  }

  override fun onDestroy() {
    tts?.stop()
    tts?.shutdown()
    super.onDestroy()
  }
}

