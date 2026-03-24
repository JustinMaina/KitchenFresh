package com.example.foodbanklocator.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.foodbanklocator.FoodBank
import com.example.foodbanklocator.FirestoreRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.*

// -------------------------------------------------------
// Distance helper — Haversine formula
// Returns distance in kilometres between two coordinates
// -------------------------------------------------------
fun distanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2).pow(2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

fun formatDistance(km: Double): String = when {
    km < 1.0 -> "${(km * 1000).toInt()} m"
    else     -> "${"%.1f".format(km)} km"
}

// -------------------------------------------------------
// MapScreen
// -------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    onFoodBankSelected: (String) -> Unit,
    onAdminLogin: () -> Unit
) {
    val context       = LocalContext.current
    val scope         = rememberCoroutineScope()
    val repository    = remember { FirestoreRepository() }
    val auth          = remember { FirebaseAuth.getInstance() }
    val fusedLocation = remember { LocationServices.getFusedLocationProviderClient(context) }

    // --------------------------------------------------------
    // State
    // --------------------------------------------------------
    val allFoodBanks by repository.foodBanksFlow().collectAsState(initial = emptyList())
    var userLocation    by remember { mutableStateOf<LatLng?>(null) }
    var searchQuery     by remember { mutableStateOf("") }
    var hasLocationPerm by remember { mutableStateOf(false) }
    var isLocating      by remember { mutableStateOf(false) }
    var showAddDialog   by remember { mutableStateOf(false) }
    var newMarkerPos    by remember { mutableStateOf<LatLng?>(null) }
    var newBankName     by remember { mutableStateOf("") }
    var newBankAddress  by remember { mutableStateOf("") }
    var newBankPhone    by remember { mutableStateOf("") }
    var newBankHours    by remember { mutableStateOf("") }
    var newBankItems    by remember { mutableStateOf("") }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf<String?>(null) }

    val isLoggedIn = auth.currentUser != null

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-1.286389, 36.817223), 12f)
    }

    // --------------------------------------------------------
    // Seed initial data if empty
    // --------------------------------------------------------
    LaunchedEffect(Unit) {
        val existing = repository.getAllFoodBanks()
        if (existing.isEmpty()) repository.seedInitialData()
    }

    // --------------------------------------------------------
    // Location permission launcher
    // --------------------------------------------------------
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPerm = granted
        if (granted) {
            isLocating = true
            scope.launch {
                try {
                    val location = fusedLocation.lastLocation.await()
                    location?.let {
                        userLocation = LatLng(it.latitude, it.longitude)
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(userLocation!!, 14f)
                        )
                    }
                } catch (e: Exception) {
                    errorMessage = "Could not get location. Please try again."
                } finally {
                    isLocating = false
                }
            }
        } else {
            errorMessage = "Location permission denied. Enable it in Settings."
        }
    }

    // --------------------------------------------------------
    // Filter + sort food banks
    // --------------------------------------------------------
    val filteredFoodBanks = remember(allFoodBanks, searchQuery, userLocation) {
        val query = searchQuery.trim().lowercase()
        val filtered = if (query.isBlank()) {
            allFoodBanks
        } else {
            allFoodBanks.filter { bank ->
                bank.name.lowercase().contains(query) ||
                        bank.address.lowercase().contains(query) ||
                        bank.items.any { it.lowercase().contains(query) }
            }
        }
        // Sort by distance if we have user location, otherwise alphabetically
        if (userLocation != null) {
            filtered.sortedBy {
                distanceKm(userLocation!!.latitude, userLocation!!.longitude,
                    it.latitude, it.longitude)
            }
        } else {
            filtered.sortedBy { it.name }
        }
    }

    // --------------------------------------------------------
    // UI
    // --------------------------------------------------------
    Box(modifier = Modifier.fillMaxSize()) {

        // ------------------------------------------------
        // Google Map
        // ------------------------------------------------
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                if (isLoggedIn) {
                    newMarkerPos = latLng
                    newBankName  = ""
                }
            },
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPerm
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false, // We use our own button
                zoomControlsEnabled     = false
            )
        ) {
            filteredFoodBanks.forEach { bank ->
                val position = LatLng(bank.latitude, bank.longitude)
                val distance = userLocation?.let {
                    formatDistance(distanceKm(it.latitude, it.longitude,
                        bank.latitude, bank.longitude))
                }
                key(bank.id) {
                    Marker(
                        state   = rememberMarkerState(position = position),
                        title   = bank.name,
                        snippet = distance ?: bank.address.ifBlank { bank.openingHours },
                        icon    = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_ORANGE
                        ),
                        onClick = {
                            onFoodBankSelected(bank.id)
                            true
                        }
                    )
                }
            }

            // Preview marker for new food bank (admins only)
            newMarkerPos?.let { pos ->
                Marker(
                    state   = rememberMarkerState(position = pos),
                    title   = "New food bank here",
                    icon    = BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_GREEN
                    )
                )
            }
        }

        // ------------------------------------------------
        // Top search bar
        // ------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search by name, area or item...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor      = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor    = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            // Results count badge
            if (searchQuery.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = "${filteredFoodBanks.size} result${if (filteredFoodBanks.size != 1) "s" else ""} found",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // ------------------------------------------------
        // Right side action buttons
        // ------------------------------------------------
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // GPS / Find Near Me button
            FloatingActionButton(
                onClick = {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor   = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                if (isLocating) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(Icons.Default.MyLocation, contentDescription = "Find near me")
                }
            }
        }

        // ------------------------------------------------
        // Bottom bar — admin controls + food bank count
        // ------------------------------------------------
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color  = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
            shadowElevation = 8.dp,
            shape  = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Food bank count
                Column {
                    Text(
                        text  = "${filteredFoodBanks.size} food bank${if (filteredFoodBanks.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text  = if (userLocation != null) "sorted by distance"
                        else "tap a marker for details",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Admin controls
                if (isLoggedIn) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Add food bank button
                        Button(
                            onClick = { showAddDialog = true },
                            shape  = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add", style = MaterialTheme.typography.labelLarge)
                        }

                        // Logout button
                        OutlinedButton(
                            onClick = {
                                auth.signOut()
                                newMarkerPos = null
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Sign out", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                } else {
                    // Not logged in — show admin login prompt
                    TextButton(onClick = onAdminLogin) {
                        Text(
                            text  = "Manage a food bank?",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // ------------------------------------------------
        // Error message
        // ------------------------------------------------
        errorMessage?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp, start = 16.dp, end = 16.dp),
                action = {
                    TextButton(onClick = { errorMessage = null }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(msg, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }

    // --------------------------------------------------------
    // Add Food Bank Dialog (tap on map while logged in)
    // --------------------------------------------------------
    if (showAddDialog || newMarkerPos != null) {
        val pos = newMarkerPos
        AlertDialog(
            onDismissRequest = {
                showAddDialog  = false
                newMarkerPos   = null
                newBankName    = ""
                newBankAddress = ""
                newBankPhone   = ""
                newBankHours   = ""
                newBankItems   = ""
            },
            title = { Text("Add New Food Bank") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Name
                    OutlinedTextField(
                        value = newBankName,
                        onValueChange = { newBankName = it },
                        label  = { Text("Food bank name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Address
                    OutlinedTextField(
                        value = newBankAddress,
                        onValueChange = { newBankAddress = it },
                        label  = { Text("Address *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Phone
                    OutlinedTextField(
                        value = newBankPhone,
                        onValueChange = { newBankPhone = it },
                        label  = { Text("Phone number *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Opening hours
                    OutlinedTextField(
                        value = newBankHours,
                        onValueChange = { newBankHours = it },
                        label  = { Text("Opening hours *") },
                        placeholder = { Text("e.g. Mon–Fri 08:00–17:00") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Available items
                    OutlinedTextField(
                        value = newBankItems,
                        onValueChange = { newBankItems = it },
                        label  = { Text("Available items") },
                        placeholder = { Text("e.g. flour, sugar, rice") },
                        singleLine = false,
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Location info
                    if (pos != null) {
                        Text(
                            "📍 Location: ${"%.4f".format(pos.latitude)}, ${"%.4f".format(pos.longitude)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            "📍 Location will use your current map view center",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        "* Required fields",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBankName.isNotBlank() &&
                            newBankAddress.isNotBlank() &&
                            newBankPhone.isNotBlank() &&
                            newBankHours.isNotBlank()) {
                            val finalPos = pos
                                ?: userLocation
                                ?: cameraPositionState.position.target
                            scope.launch {
                                isLoading = true
                                repository.addFoodBank(
                                    FoodBank(
                                        name         = newBankName,
                                        latitude     = finalPos.latitude,
                                        longitude    = finalPos.longitude,
                                        address      = newBankAddress,
                                        phone        = newBankPhone,
                                        openingHours = newBankHours,
                                        items        = newBankItems
                                            .split(",")
                                            .map { it.trim() }
                                            .filter { it.isNotBlank() }
                                    )
                                )
                                isLoading      = false
                                showAddDialog  = false
                                newMarkerPos   = null
                                newBankName    = ""
                                newBankAddress = ""
                                newBankPhone   = ""
                                newBankHours   = ""
                                newBankItems   = ""
                            }
                        }
                    },
                    enabled = newBankName.isNotBlank() &&
                            newBankAddress.isNotBlank() &&
                            newBankPhone.isNotBlank() &&
                            newBankHours.isNotBlank() &&
                            !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Add Food Bank")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog  = false
                    newMarkerPos   = null
                    newBankName    = ""
                    newBankAddress = ""
                    newBankPhone   = ""
                    newBankHours   = ""
                    newBankItems   = ""
                }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
