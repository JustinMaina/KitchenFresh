package com.example.foodbanklocator.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// -------------------------------------------------------
// Screen Placeholders
// -------------------------------------------------------
// These are TEMPORARY composables that stand in for the
// real screens while we build them one by one.
// Each placeholder will be deleted and replaced as we
// complete LandingScreen, LoginScreen, MapScreen, and
// DetailScreen in the coming steps.
// -------------------------------------------------------

@Composable
fun LandingScreenPlaceholder(
    onGetStarted: () -> Unit,
    onAdminLogin: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("KitchenFresh", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(16.dp))
            Text("Landing Screen — coming soon",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))
            Button(onClick = onGetStarted) { Text("Get Started") }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onAdminLogin) { Text("I manage a food bank") }
        }
    }
}

@Composable
fun LoginScreenPlaceholder(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Admin Login", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Text("Login Screen — coming soon",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))
            Button(onClick = onLoginSuccess) { Text("Simulate Login") }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onBack) { Text("Back") }
        }
    }
}

@Composable
fun MapScreenPlaceholder(
    onFoodBankSelected: (String) -> Unit,
    onAdminLogin: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Map Screen", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Text("Map Screen — coming soon",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))
            Button(onClick = { onFoodBankSelected("test-id") }) {
                Text("Simulate selecting a food bank")
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onAdminLogin) { Text("Admin Login") }
        }
    }
}

@Composable
fun DetailScreenPlaceholder(
    foodBankId: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Detail Screen", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Text("Food Bank ID: $foodBankId",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))
            Button(onClick = onBack) { Text("Back to Map") }
        }
    }
}
