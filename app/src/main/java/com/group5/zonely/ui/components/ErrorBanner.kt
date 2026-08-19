package com.group5.zonely.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.group5.zonely.domain.model.ThemeMode
import com.group5.zonely.ui.theme.LocalSpacing
import com.group5.zonely.ui.theme.ZonelyTheme

@Composable
fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    val spacing = LocalSpacing.current
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = spacing.md)
            )
            action?.invoke()
        }
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ErrorBannerPreview() {
    ZonelyTheme(themeMode = ThemeMode.SYSTEM, dynamicColor = false) {
        ErrorBanner(
            message = "Location services are off. Turn them on to track your zones.",
            action = {
                TextButton(onClick = {}) {
                    Text("Turn on")
                }
            }
        )
    }
}
