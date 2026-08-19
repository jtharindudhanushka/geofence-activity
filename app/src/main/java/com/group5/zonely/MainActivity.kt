package com.group5.zonely

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.group5.zonely.ui.navigation.ZonelyNavHost
import com.group5.zonely.ui.theme.ZonelyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // S0: SettingsRepository will provide the theme mode
            // For now, using SYSTEM as default until contracts are in place
            ZonelyTheme {
                ZonelyNavHost()
            }
        }
    }
}
