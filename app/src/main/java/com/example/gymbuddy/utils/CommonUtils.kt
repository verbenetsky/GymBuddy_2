package com.example.gymbuddy.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.example.gymbuddy.R

object CommonUtils {
    @Composable
    fun logoTheme (): Int {
        val isDarkTheme = isSystemInDarkTheme()
        return if (isDarkTheme) {
            R.drawable.logo_gymbuddy_black_theme
        } else {
            R.drawable.logo_gymbuddy_white_theme
        }
    }

}