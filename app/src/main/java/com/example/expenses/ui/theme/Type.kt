package com.example.expenses.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.expenses.R // Make sure this import is correct

// Set up the provider to download fonts from Google.
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Define the "Nunito" font.
val NunitoFont = GoogleFont("Nunito")

// Create a FontFamily that can be used in the Typography styles.
val NunitoFontFamily = FontFamily(
    Font(googleFont = NunitoFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = NunitoFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = NunitoFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = NunitoFont, fontProvider = provider, weight = FontWeight.Bold)
)

// Define the Typography object that MaterialTheme will use.
val Typography = Typography(
    // Apply the new font family to all the text styles
    bodyLarge = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    // Apply the font family to other styles as well to ensure consistency.
    bodyMedium = TextStyle(fontFamily = NunitoFontFamily),
    bodySmall = TextStyle(fontFamily = NunitoFontFamily),
    displayLarge = TextStyle(fontFamily = NunitoFontFamily),
    displayMedium = TextStyle(fontFamily = NunitoFontFamily),
    displaySmall = TextStyle(fontFamily = NunitoFontFamily),
    headlineLarge = TextStyle(fontFamily = NunitoFontFamily),
    headlineMedium = TextStyle(fontFamily = NunitoFontFamily),
    headlineSmall = TextStyle(fontFamily = NunitoFontFamily),
    labelLarge = TextStyle(fontFamily = NunitoFontFamily),
    labelMedium = TextStyle(fontFamily = NunitoFontFamily),
    titleMedium = TextStyle(fontFamily = NunitoFontFamily),
    titleSmall = TextStyle(fontFamily = NunitoFontFamily)
)
