package com.group5.zonely.ui.onboarding

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.group5.zonely.R
import com.group5.zonely.domain.model.PermissionState
import com.group5.zonely.ui.theme.ZonelyTheme

@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val permissionState by viewModel.permissionState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var welcomeAcknowledged by remember { mutableStateOf(false) }
    val apiLevel = Build.VERSION.SDK_INT

    val currentStep = remember(permissionState, welcomeAcknowledged) {
        OnboardingViewModel.resolveCurrentStep(permissionState, apiLevel, welcomeAcknowledged)
    }

    // Refresh permissions when returning to the app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(currentStep) {
        if (currentStep == OnboardingStep.COMPLETED) {
            viewModel.completeOnboarding(onFinished)
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (currentStep) {
                OnboardingStep.WELCOME -> WelcomeStep(onStart = { welcomeAcknowledged = true })
                OnboardingStep.FOREGROUND_LOCATION -> ForegroundLocationStep(permissionState, onContinueLimited = onFinished)
                OnboardingStep.BACKGROUND_LOCATION -> BackgroundLocationStep(permissionState, onContinueLimited = onFinished)
                OnboardingStep.NOTIFICATIONS -> NotificationStep(permissionState, onContinueLimited = onFinished)
                OnboardingStep.LOCATION_SERVICES -> LocationServicesStep(permissionState, onContinueLimited = onFinished)
                OnboardingStep.COMPLETED -> Box(Modifier.fillMaxSize()) // Transitioning
            }
        }
    }
}

@Composable
fun WelcomeStep(onStart: () -> Unit) {
    Column(
        modifier = Modifier.semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.onboarding_welcome_desc),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(stringResource(R.string.onboarding_get_started))
        }
    }
}

@Composable
fun ForegroundLocationStep(state: PermissionState, onContinueLimited: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    val showRationale = activity?.let {
        ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION)
    } ?: false

    Text(text = stringResource(R.string.onboarding_fg_title), style = MaterialTheme.typography.headlineLarge)
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = stringResource(R.string.onboarding_fg_desc), textAlign = TextAlign.Center)
    
    if (showRationale) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.onboarding_fg_rationale), 
             color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
    }

    Spacer(modifier = Modifier.height(32.dp))
    
    if (!showRationale) {
        Button(onClick = {
            launcher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }) {
            Text(stringResource(R.string.onboarding_grant_permission))
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                launcher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }) {
                Text(stringResource(R.string.onboarding_grant_permission))
            }
            OutlinedButton(onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }) {
                Text(stringResource(R.string.onboarding_open_settings))
            }
        }
    }
    
    TextButton(onClick = onContinueLimited) {
        Text(stringResource(R.string.onboarding_continue_limited))
    }
}

@Composable
fun BackgroundLocationStep(state: PermissionState, onContinueLimited: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    Text(text = stringResource(R.string.onboarding_bg_title), style = MaterialTheme.typography.headlineLarge)
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = stringResource(R.string.onboarding_bg_desc), textAlign = TextAlign.Center)
    
    if (Build.VERSION.SDK_INT >= 30) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.onboarding_bg_api30_instruction), 
             style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
    }
    
    Spacer(modifier = Modifier.height(32.dp))
    
    Button(onClick = {
        if (Build.VERSION.SDK_INT >= 30) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        } else {
            launcher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }) {
        Text(if (Build.VERSION.SDK_INT >= 30) stringResource(R.string.onboarding_open_settings) else stringResource(R.string.onboarding_grant_permission))
    }

    TextButton(onClick = onContinueLimited) {
        Text(stringResource(R.string.onboarding_continue_limited))
    }
}

@Composable
fun NotificationStep(state: PermissionState, onContinueLimited: () -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    Text(text = stringResource(R.string.onboarding_notif_title), style = MaterialTheme.typography.headlineLarge)
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = stringResource(R.string.onboarding_notif_desc), textAlign = TextAlign.Center)
    Spacer(modifier = Modifier.height(32.dp))

    Button(onClick = {
        if (Build.VERSION.SDK_INT >= 33) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }) {
        Text(stringResource(R.string.onboarding_grant_permission))
    }

    TextButton(onClick = onContinueLimited) {
        Text(stringResource(R.string.onboarding_continue_limited))
    }
}

@Composable
fun LocationServicesStep(state: PermissionState, onContinueLimited: () -> Unit) {
    val context = LocalContext.current
    Text(text = stringResource(R.string.onboarding_loc_services_title), style = MaterialTheme.typography.headlineLarge)
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = stringResource(R.string.onboarding_loc_services_desc), textAlign = TextAlign.Center)
    Spacer(modifier = Modifier.height(32.dp))

    Button(onClick = {
        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }) {
        Text(stringResource(R.string.onboarding_enable_location))
    }

    TextButton(onClick = onContinueLimited) {
        Text(stringResource(R.string.onboarding_continue_limited))
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeStepPreview() {
    ZonelyTheme {
        WelcomeStep(onStart = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ForegroundStepPreview() {
    ZonelyTheme {
        ForegroundLocationStep(
            state = PermissionState(false, false, false, false, false),
            onContinueLimited = {}
        )
    }
}
