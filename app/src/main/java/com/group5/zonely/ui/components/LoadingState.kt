@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.group5.zonely.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.group5.zonely.domain.model.ThemeMode
import com.group5.zonely.ui.theme.ZonelyTheme

@Composable
fun LoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // TODO(M3E): Use LoadingIndicator when library version is confirmed
        CircularProgressIndicator()
        // LoadingIndicator()
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoadingStatePreview() {
    ZonelyTheme(themeMode = ThemeMode.SYSTEM, dynamicColor = false) {
        androidx.compose.material3.Surface {
            LoadingState()
        }
    }
}
