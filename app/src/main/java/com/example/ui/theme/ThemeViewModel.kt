package com.example.ui.theme

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences("perennial_settings", Context.MODE_PRIVATE)
    
    private val _isDarkTheme = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        val newValue = !_isDarkTheme.value
        setDarkTheme(newValue)
    }

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        sharedPrefs.edit().putBoolean("dark_mode", isDark).apply()
        
        try {
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val themeStr = if (isDark) "Dark (Ink Teal)" else "Light (Sage)"
                db.collection("users").document(currentUser.uid)
                    .update("theme", themeStr)
            }
        } catch (e: Exception) {
            // Fail silently if Firebase is not initialized or offline
        }
    }
}
