package com.group5.zonely.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.group5.zonely.domain.model.ThemeMode
import com.group5.zonely.ui.theme.LocalSpacing
import com.group5.zonely.ui.theme.ZonelyTheme

@Composable
fun StatusPill(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.full,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.xs)
        )
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StatusPillPreview() {
    ZonelyTheme(themeMode = ThemeMode.SYSTEM, dynamicColor = false) {
        StatusPill(
            text = "INSIDE",
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
