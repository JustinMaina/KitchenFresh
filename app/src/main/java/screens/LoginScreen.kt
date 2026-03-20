package com.example.foodbanklocator.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.foodbanklocator.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    // Form state
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // UI state
    var isLoading      by remember { mutableStateOf(false) }
    var errorMessage   by remember { mutableStateOf<String?>(null) }
    var isRegisterMode by remember { mutableStateOf(false) }

    // --------------------------------------------------------
    // Email/Password Auth
    // --------------------------------------------------------

    fun handleLogin() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please enter your email and password."
            return
        }
        isLoading = true
        errorMessage = null
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener {
                isLoading = false
                onLoginSuccess()
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = when (e) {
                    is FirebaseAuthInvalidUserException        -> "No account found with this email."
                    is FirebaseAuthInvalidCredentialsException -> "Incorrect password. Please try again."
                    else                                       -> "Login failed. Check your connection and try again."
                }
            }
    }

    fun handleRegister() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please enter your email and password."
            return
        }
        if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters."
            return
        }
        isLoading = true
        errorMessage = null
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener {
                isLoading = false
                onLoginSuccess()
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = when (e) {
                    is FirebaseAuthWeakPasswordException  -> "Password is too weak. Use at least 6 characters."
                    is FirebaseAuthUserCollisionException -> "An account with this email already exists."
                    else                                  -> "Registration failed. Check your connection and try again."
                }
            }
    }

    // --------------------------------------------------------
    // Google Sign-In
    // --------------------------------------------------------

    fun handleGoogleSignIn() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId("714725858995-uj8t7ds8r1mh59a8p380p4dr8mu8opbs.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val googleIdToken = GoogleIdTokenCredential
                    .createFrom(result.credential.data)
                    .idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

                auth.signInWithCredential(firebaseCredential)
                    .addOnSuccessListener {
                        isLoading = false
                        onLoginSuccess()
                    }
                    .addOnFailureListener {
                        isLoading = false
                        errorMessage = "Google Sign-In failed. Please try again."
                    }

            } catch (e: GetCredentialException) {
                isLoading = false
                errorMessage = "Google Sign-In cancelled or unavailable."
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Something went wrong. Please try again."
            }
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
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                )
            )
    ) {

        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(Modifier.height(80.dp))

            // Title
            Text(
                text = if (isRegisterMode) "Create Account" else "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Subtitle
            Text(
                text = if (isRegisterMode)
                    "Register to manage your food bank listing"
                else
                    "Sign in to manage your food bank listing",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            // ------------------------------------------------
            // Email field
            // ------------------------------------------------
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text("Email address") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedLabelColor    = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(Modifier.height(16.dp))

            // ------------------------------------------------
            // Password field
            // ------------------------------------------------
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.VisibilityOff
                            else
                                Icons.Default.Visibility,
                            contentDescription = if (passwordVisible)
                                "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (isRegisterMode) handleRegister() else handleLogin()
                    }
                ),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedLabelColor    = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(Modifier.height(12.dp))

            // ------------------------------------------------
            // Error message
            // ------------------------------------------------
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ------------------------------------------------
            // Sign In / Register button
            // ------------------------------------------------
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (isRegisterMode) handleRegister() else handleLogin()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(22.dp),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text  = if (isRegisterMode) "Create Account" else "Sign In",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ------------------------------------------------
            // Divider with "or continue with"
            // ------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
                Text(
                    text  = "  or continue with  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            }

            Spacer(Modifier.height(20.dp))

            // ------------------------------------------------
            // Google Sign-In button
            // ------------------------------------------------
            OutlinedButton(
                onClick = {
                    focusManager.clearFocus()
                    handleGoogleSignIn()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            ) {
                // Actual Google G logo from drawable
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google logo",
                    modifier = Modifier.size(22.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text  = "Continue with Google",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(20.dp))

            // ------------------------------------------------
            // Toggle between login and register
            // ------------------------------------------------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text  = if (isRegisterMode) "Already have an account?" else "New food bank manager?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = {
                    isRegisterMode = !isRegisterMode
                    errorMessage   = null
                }) {
                    Text(
                        text  = if (isRegisterMode) "Sign In" else "Register",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
