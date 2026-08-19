package com.group5.zonely.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.group5.zonely.domain.model.GeofenceZone
import com.group5.zonely.domain.model.ThemeMode
import com.group5.zonely.domain.model.TransitionType
import com.group5.zonely.ui.theme.LocalSpacing
import com.group5.zonely.ui.theme.ZonelyTheme

@Composable
fun ZoneCard(
    zone: GeofenceZone,
    onActiveChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    ListItem(
        headlineContent = { Text(text = zone.name) },
        supportingContent = {
            Column {
                Text(text = "${zone.radiusMeters.toInt()} m")
                Row(
                    modifier = Modifier.padding(top = spacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    zone.transitionTypes.forEach { type ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(text = type.name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(Color(zone.colorArgb))
            )
        },
        trailingContent = {
            Switch(
                checked = zone.isActive,
                onCheckedChange = onActiveChanged
            )
        },
        modifier = modifier
    )
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ZoneCardPreview() {
    val mockZone = GeofenceZone(
        id = "1",
        name = "Home Zone",
        latitude = 0.0,
        longitude = 0.0,
        radiusMeters = 200f,
        transitionTypes = setOf(TransitionType.ENTER, TransitionType.EXIT),
        colorArgb = 0xFFE91E63.toInt(),
        createdAt = 0,
        updatedAt = 0,
        isActive = true
    )
    ZonelyTheme(themeMode = ThemeMode.SYSTEM, dynamicColor = false) {
        Surface {
            ZoneCard(
                zone = mockZone,
                onActiveChanged = {}
            )
        }
    }
}
