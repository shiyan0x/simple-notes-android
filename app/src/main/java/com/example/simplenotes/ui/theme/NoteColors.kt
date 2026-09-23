package com.example.simplenotes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class NoteColorTheme(
    val key: String,
    val name: String,
    val lightBg: Color,
    val lightBorder: Color,
    val darkBg: Color,
    val darkBorder: Color
)

object NoteColors {

    val palette = listOf(
        NoteColorTheme(
            key = "DEFAULT",
            name = "Default",
            lightBg = Color(0xFFFFFFFF),
            lightBorder = Color(0xFFE2E8F0),
            darkBg = Color(0xFF1E293B),
            darkBorder = Color(0xFF334155)
        ),
        NoteColorTheme(
            key = "RED",
            name = "Red",
            lightBg = Color(0xFFFFEBEE),
            lightBorder = Color(0xFFFFCDD2),
            darkBg = Color(0xFF3E1A1D),
            darkBorder = Color(0xFF5C2327)
        ),
        NoteColorTheme(
            key = "ORANGE",
            name = "Orange",
            lightBg = Color(0xFFFFF3E0),
            lightBorder = Color(0xFFFFE0B2),
            darkBg = Color(0xFF3E2718),
            darkBorder = Color(0xFF5C3820)
        ),
        NoteColorTheme(
            key = "YELLOW",
            name = "Yellow",
            lightBg = Color(0xFFFFFDE7),
            lightBorder = Color(0xFFFFF9C4),
            darkBg = Color(0xFF3A3816),
            darkBorder = Color(0xFF595622)
        ),
        NoteColorTheme(
            key = "GREEN",
            name = "Green",
            lightBg = Color(0xE8F5E9FF.toInt()),
            lightBorder = Color(0xFFC8E6C9),
            darkBg = Color(0xFF1A331E),
            darkBorder = Color(0xFF274D32)
        ),
        NoteColorTheme(
            key = "BLUE",
            name = "Blue",
            lightBg = Color(0xFFE3F2FD),
            lightBorder = Color(0xFFBBDEFB),
            darkBg = Color(0xFF162C3D),
            darkBorder = Color(0xFF23445C)
        ),
        NoteColorTheme(
            key = "PURPLE",
            name = "Purple",
            lightBg = Color(0xFFF3E5F5),
            lightBorder = Color(0xFFE1BEE7),
            darkBg = Color(0xFF2F1A3A),
            darkBorder = Color(0xFF48275A)
        )
    )

    @Composable
    fun getBackgroundColor(colorKey: String, isDark: Boolean): Color {
        val theme = palette.find { it.key == colorKey } ?: palette.first()
        return if (isDark) theme.darkBg else theme.lightBg
    }

    @Composable
    fun getBorderColor(colorKey: String, isDark: Boolean): Color {
        val theme = palette.find { it.key == colorKey } ?: palette.first()
        return if (isDark) theme.darkBorder else theme.lightBorder
    }
}
