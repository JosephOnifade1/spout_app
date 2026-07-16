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
    }
}
