package com.polish.thousand.core.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Immutable

internal val PolishThousandLightColorScheme = lightColorScheme(
    primary = Color(0xFF2D6F67),
    onPrimary = Color(0xFFF6FBF9),
    primaryContainer = Color(0xFFD9EEE8),
    onPrimaryContainer = Color(0xFF163A36),
    secondary = Color(0xFFC9785C),
    onSecondary = Color(0xFFFFF8F5),
    secondaryContainer = Color(0xFFF5DED5),
    onSecondaryContainer = Color(0xFF4F2A1D),
    tertiary = Color(0xFFE8A54F),
    onTertiary = Color(0xFF33210A),
    background = Color(0xFFFFFCF8),
    onBackground = Color(0xFF1F211F),
    surface = Color(0xFFFFFCF8),
    onSurface = Color(0xFF1F211F),
    surfaceVariant = Color(0xFFF4EEE8),
    onSurfaceVariant = Color(0xFF6D716C),
    outline = Color(0xFFD7D0C8),
    outlineVariant = Color(0xFFEAE3DB),
    surfaceContainerHighest = Color(0xFFF5F1EA)
)

internal val PolishThousandDarkColorScheme = darkColorScheme(
    primary = Color(0xFF90D1C7),
    onPrimary = Color(0xFF093730),
    primaryContainer = Color(0xFF174D46),
    onPrimaryContainer = Color(0xFFD9EEE8),
    secondary = Color(0xFFF0B49D),
    onSecondary = Color(0xFF4A2418),
    secondaryContainer = Color(0xFF653728),
    onSecondaryContainer = Color(0xFFFCE5DC),
    tertiary = Color(0xFFF2C784),
    onTertiary = Color(0xFF49310C),
    background = Color(0xFF101514),
    onBackground = Color(0xFFE5E2DC),
    surface = Color(0xFF101514),
    onSurface = Color(0xFFE5E2DC),
    surfaceVariant = Color(0xFF1D2523),
    onSurfaceVariant = Color(0xFFBFC5BE),
    outline = Color(0xFF59605B),
    outlineVariant = Color(0xFF313835),
    surfaceContainerHighest = Color(0xFF1D2523)
)

@Immutable
data class PolishThousandExtendedColors(
    val heroStart: Color,
    val heroEnd: Color,
    val heroGlow: Color,
    val progressTrack: Color,
    val progressFill: Color,
    val chipContainer: Color,
    val chipContent: Color,
    val chipSelectedContainer: Color,
    val chipSelectedContent: Color,
    val badgeContainer: Color,
    val badgeContent: Color,
    val languageContainer: Color,
    val languageContent: Color,
    val languageSelectedContainer: Color,
    val languageSelectedContent: Color,
    val socialProofA: Color,
    val socialProofB: Color,
    val socialProofC: Color,
    val bottomHandle: Color
)

internal val PolishThousandLightExtendedColors = PolishThousandExtendedColors(
    heroStart = Color(0xFFF3FAF7),
    heroEnd = Color(0xFFFDF4EE),
    heroGlow = Color(0xFFEDF8FF),
    progressTrack = Color(0xFFE7DED4),
    progressFill = Color(0xFFC9785C),
    chipContainer = Color(0xFFF2EEE7),
    chipContent = Color(0xFF505652),
    chipSelectedContainer = Color(0xFFC9785C),
    chipSelectedContent = Color(0xFFFFFAF7),
    badgeContainer = Color(0xFFE9A84E),
    badgeContent = Color(0xFF4E3207),
    languageContainer = Color(0xFFF4EFE8),
    languageContent = Color(0xFF7A7E78),
    languageSelectedContainer = Color(0xFF2D6F67),
    languageSelectedContent = Color(0xFFF6FBF9),
    socialProofA = Color(0xFFFFE2D3),
    socialProofB = Color(0xFFDDEFEA),
    socialProofC = Color(0xFFF7EFC4),
    bottomHandle = Color(0xFFD3CCC4)
)

internal val PolishThousandDarkExtendedColors = PolishThousandExtendedColors(
    heroStart = Color(0xFF113631),
    heroEnd = Color(0xFF3A271E),
    heroGlow = Color(0xFF173C43),
    progressTrack = Color(0xFF2A3230),
    progressFill = Color(0xFFF0B49D),
    chipContainer = Color(0xFF1E2624),
    chipContent = Color(0xFFD4D8D2),
    chipSelectedContainer = Color(0xFFF0B49D),
    chipSelectedContent = Color(0xFF472214),
    badgeContainer = Color(0xFFF2C784),
    badgeContent = Color(0xFF49310C),
    languageContainer = Color(0xFF202826),
    languageContent = Color(0xFFAAB1AB),
    languageSelectedContainer = Color(0xFF90D1C7),
    languageSelectedContent = Color(0xFF093730),
    socialProofA = Color(0xFF5A3328),
    socialProofB = Color(0xFF214640),
    socialProofC = Color(0xFF5C4A1F),
    bottomHandle = Color(0xFF5D655F)
)
