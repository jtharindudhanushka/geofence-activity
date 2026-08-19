package com.group5.zonely

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.group5.zonely.ui.navigation.ZonelyNavHost
import com.group5.zonely.ui.theme.ZonelyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZonelyTheme {
                ZonelyNavHost()
            }
        }
    }

    // TODO: Dev D - handleIntent if needed for notifications, but maybe better inside ZonelyNavHost or a separate navigator
}
