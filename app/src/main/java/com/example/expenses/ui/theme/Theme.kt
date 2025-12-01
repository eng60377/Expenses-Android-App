package com.example.expenses.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect // Make sure this is imported
import androidx.compose.ui.graphics.toArgb // Make sure this is imported
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView // Make sure this is imported
import androidx.core.view.WindowCompat // Make sure this is imported

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreen,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = DarkGreen,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun ExpensesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // This is the part that applies your custom font and colors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // This applies the "Nunito" font from your Type.kt
        content = content
    )
}
