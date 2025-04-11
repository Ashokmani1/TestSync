package com.teksxt.closedtesting.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.teksxt.closedtesting.R

// Define brand colors for TestSync app

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Define Roboto font family
private val RobotoFont = GoogleFont("Roboto")

val RobotoFontFamily = FontFamily(
    Font(googleFont = RobotoFont, fontProvider = provider, weight = FontWeight.Thin),
    Font(googleFont = RobotoFont, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = RobotoFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = RobotoFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = RobotoFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = RobotoFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = RobotoFont, fontProvider = provider, weight = FontWeight.Black)
)


@Composable
fun TestSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {

    val LightColorScheme = lightColorScheme(
        primary = Color(0xFF4664FF), // **Primary (Main Blue)**
        onPrimary = Color(0xFFFFFFFF), // **Text on Primary**
        primaryContainer = Color(0xFFB3C3FF), // **Lighter version for backgrounds**
        onPrimaryContainer = Color(0xFF001A72), // **Darker shade for contrast**

        secondary = Color(0xFF294BC3), // **Deep Blue for accents**
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFC6D4FF),
        onSecondaryContainer = Color(0xFF001452),

        tertiary = Color(0xFF13A8A5), // **Teal for contrast**
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFA8F4F2),
        onTertiaryContainer = Color(0xFF002523),

        background = Color(0xFFF8F9FA), // **Light Gray Background**
        onBackground = Color(0xFF121212), // **Dark text for readability**

        surface = Color(0xFFFFFFFF), // **White cards and surfaces**
        onSurface = Color(0xFF000000),

        surfaceVariant = Color(0xFFE0E0E0),
        onSurfaceVariant = Color(0xFF424242),

        error = Color(0xFFD32F2F), // **Error Red**
        onError = Color(0xFFFFFFFF),

        outline = Color(0xFFBDBDBD),
        outlineVariant = Color(0xFF9E9E9E),

        inverseSurface = Color(0xFF303030),
        inverseOnSurface = Color(0xFFFFFFFF),

        //        card = Color(0xFFFFFFFF), // **Pure white cards**
        //        onCard = Color(0xFF121212) // **Dark text on cards**
    )

    val darkScheme = darkColorScheme(
        primary = primaryDark,
        onPrimary = onPrimaryDark,
        primaryContainer = primaryContainerDark,
        onPrimaryContainer = onPrimaryContainerDark,
        secondary = secondaryDark,
        onSecondary = onSecondaryDark,
        secondaryContainer = secondaryContainerDark,
        onSecondaryContainer = onSecondaryContainerDark,
        tertiary = tertiaryDark,
        onTertiary = onTertiaryDark,
        tertiaryContainer = tertiaryContainerDark,
        onTertiaryContainer = onTertiaryContainerDark,
        error = errorDark,
        onError = onErrorDark,
        errorContainer = errorContainerDark,
        onErrorContainer = onErrorContainerDark,
        background = backgroundDark,
        onBackground = onBackgroundDark,
        surface = surfaceDark,
        onSurface = onSurfaceDark,
        surfaceVariant = surfaceVariantDark,
        onSurfaceVariant = onSurfaceVariantDark,
        outline = outlineDark,
        outlineVariant = outlineVariantDark,
        scrim = scrimDark,
        inverseSurface = inverseSurfaceDark,
        inverseOnSurface = inverseOnSurfaceDark,
        inversePrimary = inversePrimaryDark,
    )



    val typography = Typography(
        displayLarge = TextStyle(
            fontFamily = RobotoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        ),
        displayMedium = TextStyle(
            fontFamily = RobotoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = RobotoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = RobotoFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        ),
        titleLarge = TextStyle(
            fontFamily = RobotoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        ),
        titleMedium = TextStyle(
            fontFamily = RobotoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = RobotoFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = RobotoFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        ),
        bodySmall = TextStyle(
            fontFamily = RobotoFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp
        ),
        labelLarge = TextStyle(
            fontFamily = RobotoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        ),
        labelMedium = TextStyle(
            fontFamily = RobotoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        ),
        labelSmall = TextStyle(
            fontFamily = RobotoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp
        )
    )

//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = LightColorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = typography,
        content = content
    )
}