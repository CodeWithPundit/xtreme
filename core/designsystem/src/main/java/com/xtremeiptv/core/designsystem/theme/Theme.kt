package com.xtremeiptv.core.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldGlow,
    onPrimary = DeepAbyss,
    primaryContainer = CursedTeal,
    onPrimaryContainer = BoneWhite,
    secondary = HoardersGold,
    onSecondary = DeepAbyss,
    secondaryContainer = SunkenTimber,
    onSecondaryContainer = BoneWhite,
    tertiary = CursedTeal,
    onTertiary = BoneWhite,
    tertiaryContainer = SunkenTimber,
    onTertiaryContainer = BoneWhite,
    background = DeepAbyss,
    onBackground = BoneWhite,
    surface = SunkenTimber,
    onSurface = BoneWhite,
    surfaceVariant = AbyssLight,
    onSurfaceVariant = WhiteBone,
    error = Error,
    onError = BoneWhite,
    errorContainer = Error.copy(alpha = 0.1f),
    onErrorContainer = Error,
    outline = TimberLight,
    inverseSurface = BoneWhite,
    inverseOnSurface = DeepAbyss,
    inversePrimary = EmeraldGlow.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldGlow,
    onPrimary = DeepAbyss,
    primaryContainer = CursedTeal,
    onPrimaryContainer = BoneWhite,
    secondary = HoardersGold,
    onSecondary = DeepAbyss,
    secondaryContainer = SunkenTimber,
    onSecondaryContainer = BoneWhite,
    tertiary = CursedTeal,
    onTertiary = BoneWhite,
    tertiaryContainer = SunkenTimber,
    onTertiaryContainer = BoneWhite,
    background = DeepAbyss,
    onBackground = BoneWhite,
    surface = SunkenTimber,
    onSurface = BoneWhite,
    surfaceVariant = AbyssLight,
    onSurfaceVariant = WhiteBone,
    error = Error,
    onError = BoneWhite,
    errorContainer = Error.copy(alpha = 0.1f),
    onErrorContainer = Error,
    outline = TimberLight,
    inverseSurface = BoneWhite,
    inverseOnSurface = DeepAbyss,
    inversePrimary = EmeraldGlow.copy(alpha = 0.5f)
)

@Composable
fun XtremeIPTVTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
            window.statusBarColor = DeepAbyss.toArgb()
            window.navigationBarColor = DeepAbyss.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

// Typography
val Typography = Typography(
    displayLarge = MaterialTheme.typography.displayLarge.copy(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold
    ),
    displayMedium = MaterialTheme.typography.displayMedium.copy(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold
    ),
    displaySmall = MaterialTheme.typography.displaySmall.copy(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold
    ),
    headlineLarge = MaterialTheme.typography.headlineLarge.copy(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = MaterialTheme.typography.headlineMedium.copy(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold
    ),
    headlineSmall = MaterialTheme.typography.headlineSmall.copy(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold
    ),
    titleLarge = MaterialTheme.typography.titleLarge.copy(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium
    ),
    titleMedium = MaterialTheme.typography.titleMedium.copy(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium
    ),
    titleSmall = MaterialTheme.typography.titleSmall.copy(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium
    ),
    bodyLarge = MaterialTheme.typography.bodyLarge.copy(
        fontFamily = Inter
    ),
    bodyMedium = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = Inter
    ),
    bodySmall = MaterialTheme.typography.bodySmall.copy(
        fontFamily = Inter
    ),
    labelLarge = MaterialTheme.typography.labelLarge.copy(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium
    ),
    labelMedium = MaterialTheme.typography.labelMedium.copy(
        fontFamily = Inter
    ),
    labelSmall = MaterialTheme.typography.labelSmall.copy(
        fontFamily = Inter
    )
)

// Shapes
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// Fonts
val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)
