package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AvatarPreset(
    val id: String,
    val emoji: String,
    val gradientStart: Color,
    val gradientEnd: Color
)

object AvatarHelper {
    val presets = listOf(
        AvatarPreset("avatar_1", "🦊", Color(0xFFF97316), Color(0xFFEF4444)), // Orange-Red Fox
        AvatarPreset("avatar_2", "🐯", Color(0xFFF59E0B), Color(0xFFD97706)), // Amber Tiger
        AvatarPreset("avatar_3", "🐼", Color(0xFF10B981), Color(0xFF047857)), // Emerald Panda
        AvatarPreset("avatar_4", "🦁", Color(0xFFEAB308), Color(0xFFCA8A04)), // Yellow Lion
        AvatarPreset("avatar_5", "🦄", Color(0xFF8B5CF6), Color(0xFF6D28D9)), // Indigo Unicorn
        AvatarPreset("avatar_6", "🐨", Color(0xFF14B8A6), Color(0xFF0F766E)), // Teal Koala
        AvatarPreset("avatar_7", "🦉", Color(0xFFEC4899), Color(0xFFBE185D)), // Pink Owl
        AvatarPreset("avatar_8", "🐳", Color(0xFF06B6D4), Color(0xFF0369A1))  // Cyan Whale
    )

    fun getPreset(id: String): AvatarPreset {
        return presets.find { it.id == id } ?: presets[0]
    }
}

@Composable
fun AvatarView(
    avatarId: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val preset = AvatarHelper.getPreset(avatarId)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(preset.gradientStart, preset.gradientEnd)
                )
            )
            .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val fontSize = (size.value * 0.5f).sp
        Text(
            text = preset.emoji,
            fontSize = fontSize
        )
    }
}
