package com.smartsubscription.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.smartsubscription.R
import com.smartsubscription.data.ThemeMode

/**
 * The design's tokens (design/ui-refinement-2026-09-20.html). The ink block is the same navy in both
 * themes; the ground and cards change. Orange means "soon", teal means "yearly", red means danger.
 */
@Immutable
data class SsColors(
    val bg: Color,
    val card: Color,
    val card2: Color,
    val line: Color,
    val text: Color,
    val text2: Color,
    val ink: Color = Color(0xFF0A2A66),
    val ink2: Color = Color(0xFF163C8F),
    val onInk: Color = Color.White,
    val onInk2: Color = Color(0xFFA9BEE8),
    val onInkAccent: Color = Color(0xFFFFB340),
    val onInkYearly: Color = Color(0xFF5EEAD4),
    val yearly: Color,
    val brand: Color,
    val brandTint: Color,
    val onBrand: Color,
    val accent: Color,
    val accentText: Color,
    val danger: Color,
    val skeleton: Color,
    val isDark: Boolean,
)

val LightSs = SsColors(
    bg = Color(0xFFF3F4F8), card = Color.White, card2 = Color(0xFFEEF1F7), line = Color(0xFFDFE3EC),
    text = Color(0xFF0B1220), text2 = Color(0xFF5B6478), yearly = Color(0xFF0F766E),
    brand = Color(0xFF0066CC), brandTint = Color(0xFFE4EEFB), onBrand = Color.White,
    accent = Color(0xFFF97316), accentText = Color(0xFFC2410C), danger = Color(0xFFD70015),
    skeleton = Color(0xFFE3E6EE), isDark = false,
)

val DarkSs = SsColors(
    bg = Color(0xFF070D1A), card = Color(0xFF101A2E), card2 = Color(0xFF172440), line = Color(0xFF223050),
    text = Color(0xFFF2F5FA), text2 = Color(0xFFA3AFC7), yearly = Color(0xFF2DD4BF),
    brand = Color(0xFF4D9FFF), brandTint = Color(0xFF14305C), onBrand = Color(0xFF06203F),
    accent = Color(0xFFFF9F0A), accentText = Color(0xFFFFB340), danger = Color(0xFFFF6B6B),
    skeleton = Color(0xFF1B2A47), isDark = true,
)

val LocalSs = staticCompositionLocalOf { LightSs }

/** Bricolage Grotesque sets amounts, dates and titles; everything else stays in the system face. */
val Display = FontFamily(
    Font(R.font.bricolage_500, FontWeight.Medium),
    Font(R.font.bricolage_700, FontWeight.Bold),
    Font(R.font.bricolage_800, FontWeight.ExtraBold),
)

object SsType {
    val big = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 56.sp, lineHeight = 56.sp, letterSpacing = (-0.035).em)
    val title = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 38.sp, letterSpacing = (-0.02).em)
    val screenTitle = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = (-0.02).em)
    val amount = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 19.sp, letterSpacing = (-0.02).em)
    val day = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 20.sp, letterSpacing = (-0.02).em)
    val group = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = (-0.01).em)
    val body = TextStyle(fontSize = 17.sp, lineHeight = 22.sp)
    val bodyStrong = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold)
    val sub = TextStyle(fontSize = 14.sp, lineHeight = 20.sp)
    val caption = TextStyle(fontSize = 13.sp, lineHeight = 18.sp)
    val eyebrow = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
    val h3 = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold)
    val sectionLabel = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
fun SmartSubscriptionTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val ss = if (dark) DarkSs else LightSs
    // Material components (dialogs, menus, pickers, switches) read these.
    val scheme = if (dark) {
        darkColorScheme(
            primary = ss.brand, onPrimary = ss.onBrand, primaryContainer = ss.brandTint,
            background = ss.bg, onBackground = ss.text, surface = ss.card, onSurface = ss.text,
            surfaceVariant = ss.card2, onSurfaceVariant = ss.text2, outline = ss.line, error = ss.danger,
            surfaceContainerHigh = ss.card, surfaceContainerHighest = ss.card2, surfaceContainer = ss.card,
        )
    } else {
        lightColorScheme(
            primary = ss.brand, onPrimary = ss.onBrand, primaryContainer = ss.brandTint,
            background = ss.bg, onBackground = ss.text, surface = ss.card, onSurface = ss.text,
            surfaceVariant = ss.card2, onSurfaceVariant = ss.text2, outline = ss.line, error = ss.danger,
            surfaceContainerHigh = ss.card, surfaceContainerHighest = ss.card2, surfaceContainer = ss.card,
        )
    }
    CompositionLocalProvider(LocalSs provides ss) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
