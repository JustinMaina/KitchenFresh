package com.example.foodbanklocator.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.foodbanklocator.FoodBank
import com.example.foodbanklocator.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    foodBankId: String,
    onBack: () -> Unit
) {
    val context    = LocalContext.current
    val scope      = rememberCoroutineScope()
    val repository = remember { FirestoreRepository() }
    val auth       = remember { FirebaseAuth.getInstance() }
    val isLoggedIn = auth.currentUser != null

    // --------------------------------------------------------
    // Load food bank data
    // --------------------------------------------------------
    var foodBank     by remember { mutableStateOf<FoodBank?>(null) }
    var isLoading    by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Edit state (admins only)
    var isEditing    by remember { mutableStateOf(false) }
    var editedName   by remember { mutableStateOf("") }
    var editedAddress by remember { mutableStateOf("") }
    var editedPhone  by remember { mutableStateOf("") }
    var editedHours  by remember { mutableStateOf("") }
    var editedItems  by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isSaving     by remember { mutableStateOf(false) }

    LaunchedEffect(foodBankId) {
        isLoading = true
        try {
            val banks = repository.getAllFoodBanks()
            foodBank = banks.find { it.id == foodBankId }
            foodBank?.let { bank ->
                editedName    = bank.name
                editedAddress = bank.address
                editedPhone   = bank.phone
                editedHours   = bank.openingHours
                editedItems   = bank.items.joinToString(", ")
            }
        } catch (e: Exception) {
            errorMessage = "Could not load food bank details."
        } finally {
            isLoading = false
        }
    }

    // --------------------------------------------------------
    // UI
    // --------------------------------------------------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color    = MaterialTheme.colorScheme.primary
                )
            }

            errorMessage != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text  = errorMessage!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBack) { Text("Go Back") }
                }
            }

            foodBank != null -> {
                val bank = foodBank!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {

                    // ----------------------------------------
                    // Header
                    // ----------------------------------------
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(top = 48.dp, bottom = 28.dp,
                                start = 20.dp, end = 20.dp)
                    ) {
                        // Back button
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        // Admin edit button
                        if (isLoggedIn) {
                            IconButton(
                                onClick = { isEditing = !isEditing },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                                    contentDescription = if (isEditing) "Cancel edit" else "Edit",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        // Food bank name
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Icon circle
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.StoreMallDirectory,
                                    contentDescription = null,
                                    tint     = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            if (isEditing) {
                                OutlinedTextField(
                                    value = editedName,
                                    onValueChange = { editedName = it },
                                    label = { Text("Food bank name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor   = MaterialTheme.colorScheme.onPrimary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                        focusedLabelColor    = MaterialTheme.colorScheme.onPrimary,
                                        unfocusedLabelColor  = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                        focusedTextColor     = MaterialTheme.colorScheme.onPrimary,
                                        unfocusedTextColor   = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            } else {
                                Text(
                                    text      = bank.name,
                                    style     = MaterialTheme.typography.titleLarge,
                                    color     = MaterialTheme.colorScheme.onPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // ----------------------------------------
                    // Details card
                    // ----------------------------------------
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape  = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {

                            // Address
                            DetailRow(
                                icon    = Icons.Default.LocationOn,
                                label   = "Address",
                                value   = bank.address.ifBlank { "Not specified" },
                                isEditing = isEditing,
                                editValue = editedAddress,
                                onEditChange = { editedAddress = it }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )

                            // Phone
                            DetailRow(
                                icon    = Icons.Default.Phone,
                                label   = "Phone",
                                value   = bank.phone.ifBlank { "Not specified" },
                                isEditing = isEditing,
                                editValue = editedPhone,
                                onEditChange = { editedPhone = it }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )

                            // Opening hours
                            DetailRow(
                                icon    = Icons.Default.AccessTime,
                                label   = "Opening hours",
                                value   = bank.openingHours.ifBlank { "Not specified" },
                                isEditing = isEditing,
                                editValue = editedHours,
                                onEditChange = { editedHours = it }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )

                            // Available items
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint     = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text  = "Available items",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    if (isEditing) {
                                        OutlinedTextField(
                                            value = editedItems,
                                            onValueChange = { editedItems = it },
                                            label = { Text("Items (comma separated)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            minLines = 2
                                        )
                                    } else if (bank.items.isNotEmpty()) {
                                        // Item chips
                                        @OptIn(ExperimentalLayoutApi::class)
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement   = Arrangement.spacedBy(6.dp)
                                        ) {
                                            bank.items.forEach { item ->
                                                Surface(
                                                    shape = RoundedCornerShape(20.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Text(
                                                        text     = item,
                                                        style    = MaterialTheme.typography.labelMedium,
                                                        color    = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.padding(
                                                            horizontal = 10.dp, vertical = 4.dp
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Text(
                                            text  = "Not specified",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ----------------------------------------
                    // Action buttons
                    // ----------------------------------------
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Get Directions button
                        Button(
                            onClick = {
                                val uri = Uri.parse(
                                    "geo:${bank.latitude},${bank.longitude}?q=${bank.latitude},${bank.longitude}(${bank.name})"
                                )
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape  = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null,
                                modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Get Directions", style = MaterialTheme.typography.titleMedium)
                        }

                        // Call button (only if phone number exists)
                        if (bank.phone.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL,
                                        Uri.parse("tel:${bank.phone}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null,
                                    modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Call ${bank.phone}",
                                    style = MaterialTheme.typography.titleMedium)
                            }
                        }

                        // ----------------------------------------
                        // Admin: Save / Delete buttons
                        // ----------------------------------------
                        AnimatedVisibility(visible = isEditing && isLoggedIn) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Save changes
                                Button(
                                    onClick = {
                                        scope.launch {
                                            isSaving = true
                                            val updates = mapOf(
                                                "name"         to editedName,
                                                "address"      to editedAddress,
                                                "phone"        to editedPhone,
                                                "openingHours" to editedHours,
                                                "items"        to editedItems
                                                    .split(",")
                                                    .map { it.trim() }
                                                    .filter { it.isNotBlank() }
                                            )
                                            repository.updateFoodBank(bank.id, updates)
                                            isSaving  = false
                                            isEditing = false
                                            // Reload
                                            val banks = repository.getAllFoodBanks()
                                            foodBank  = banks.find { it.id == foodBankId }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape   = RoundedCornerShape(14.dp),
                                    enabled = !isSaving,
                                    colors  = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    if (isSaving) {
                                        CircularProgressIndicator(
                                            modifier    = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color       = MaterialTheme.colorScheme.onSecondary
                                        )
                                    } else {
                                        Icon(Icons.Default.Save, contentDescription = null,
                                            modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Save Changes",
                                            style = MaterialTheme.typography.titleMedium)
                                    }
                                }

                                // Delete food bank
                                OutlinedButton(
                                    onClick = { showDeleteDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape  = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        width = 1.dp
                                    )
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null,
                                        modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Delete Food Bank",
                                        style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }

    // --------------------------------------------------------
    // Delete confirmation dialog
    // --------------------------------------------------------
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Food Bank") },
            text  = {
                Text("Are you sure you want to delete ${foodBank?.name}? This cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.deleteFoodBank(foodBankId)
                            showDeleteDialog = false
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// --------------------------------------------------------
// Reusable detail row — shows or edits a single field
// --------------------------------------------------------
@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    isEditing: Boolean,
    editValue: String,
    onEditChange: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint     = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            if (isEditing) {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = onEditChange,
                    singleLine = true,
                    modifier   = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text  = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
