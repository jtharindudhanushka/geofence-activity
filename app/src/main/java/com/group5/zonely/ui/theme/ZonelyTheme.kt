package com.group5.zonely.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// OWNER: Dev C - replace this entire file.
// After this commit you never touch them again.

@Composable
fun ZonelyTheme(
    // In S0, these might not be used until Dev C implements the real theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        content = content
    )
}
