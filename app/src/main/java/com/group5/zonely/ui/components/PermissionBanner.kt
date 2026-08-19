package com.group5.zonely.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.group5.zonely.R
import com.group5.zonely.domain.model.PermissionState
import com.group5.zonely.ui.theme.LocalSpacing

@Composable
fun PermissionBanner(
    state: PermissionState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val spacing = LocalSpacing.current
    
    val (text, action) = when {
        !state.locationServicesEnabled -> {
            stringResource(R.string.banner_loc_services_off) to {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
        !state.canUseForegroundLocation -> {
            stringResource(R.string.banner_missing_fg) to {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
        !state.backgroundLocationGranted -> {
            stringResource(R.string.banner_missing_bg) to {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
        !state.notificationsGranted -> {
            stringResource(R.string.banner_notif_off) to {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
        else -> null to null
    }

    if (text != null && action != null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (!state.canUseForegroundLocation || !state.locationServicesEnabled)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.tertiaryContainer
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .padding(spacing.md)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (!state.canUseForegroundLocation || !state.locationServicesEnabled)
                            Icons.Default.Error
                        else
                            Icons.Default.Warning,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(spacing.sm))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                TextButton(onClick = action) {
                    Text(stringResource(R.string.banner_fix))
                }
            }
        }
    }
}
